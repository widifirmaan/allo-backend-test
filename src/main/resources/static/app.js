

let grupAktif = null;
const KUNCI_GRUP_TERSIMPAN = "splitbill.grupTersimpan";

function formatRupiah(angka) {
  return "Rp " + Number(angka).toLocaleString("id-ID", { minimumFractionDigits: 2 });
}

function tampilkanPesan(jenis, teks) {
  document.getElementById("kotakPesan").innerHTML =
    `<div class="alert alert-${jenis} alert-dismissible fade show" role="alert">` +
    `${teks}<button type="button" class="btn-close" data-bs-dismiss="alert"></button></div>`;
}

async function panggilApi(url, opsi) {
  const response = await fetch(url, opsi);
  const badan = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(badan.message || "Terjadi kesalahan");
  }
  return badan;
}

function bacaGrupTersimpan() {
  return JSON.parse(localStorage.getItem(KUNCI_GRUP_TERSIMPAN) || "[]");
}

function simpanGrupKeDaftar(grup) {
  const daftar = bacaGrupTersimpan().filter((g) => g.groupId !== grup.groupId);
  daftar.unshift({ groupId: grup.groupId, groupName: grup.groupName });
  localStorage.setItem(KUNCI_GRUP_TERSIMPAN, JSON.stringify(daftar.slice(0, 20)));
  muatPilihanGrupTersimpan();
}

function muatPilihanGrupTersimpan() {
  const pilihan = document.getElementById("pilihGrupTersimpan");
  pilihan.innerHTML = '<option value="">— pilih —</option>';
  for (const grup of bacaGrupTersimpan()) {
    const opsi = document.createElement("option");
    opsi.value = grup.groupId;
    opsi.textContent = `${grup.groupName} (${grup.groupId.slice(0, 8)}…)`;
    pilihan.appendChild(opsi);
  }
}

async function buatGrupBaru() {
  const namaGrup = document.getElementById("inputNamaGrup").value.trim();
  const peserta = document
    .getElementById("inputPeserta")
    .value.split(",")
    .map((nama) => nama.trim())
    .filter((nama) => nama.length > 0);
  try {
    const grup = await panggilApi("/api/groups", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ groupName: namaGrup, participantNames: peserta }),
    });
    simpanGrupKeDaftar(grup);
    pilihGrupAktif(grup);
    tampilkanPesan("success", `Grup <b>${grup.groupName}</b> dibuat.`);
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

function bukaGrupTersimpan() {
  const groupId = document.getElementById("pilihGrupTersimpan").value;
  if (groupId) bukaGrupDenganId(groupId);
}

function bukaGrupManual() {
  const groupId = document.getElementById("inputGroupId").value.trim();
  if (groupId) bukaGrupDenganId(groupId);
}

async function bukaGrupDenganId(groupId) {
  try {
    const grup = await panggilApi(`/api/groups/${groupId}`);
    simpanGrupKeDaftar(grup);
    pilihGrupAktif(grup);
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

function pilihGrupAktif(grup) {
  grupAktif = grup;
  document.getElementById("peringatanBelumAdaGrup").classList.add("d-none");
  document.getElementById("formExpense").classList.remove("d-none");
  siapkanPilihanPembayar();
  siapkanCentangPeserta();
  siapkanBarisShares();
  muatUlangTampilan();
}

function siapkanPilihanPembayar() {
  const pilihan = document.getElementById("pilihPembayar");
  pilihan.innerHTML = "";
  for (const nama of grupAktif.participantNames) {
    const opsi = document.createElement("option");
    opsi.value = nama;
    opsi.textContent = nama;
    pilihan.appendChild(opsi);
  }
}

function siapkanCentangPeserta() {
  const wadah = document.getElementById("daftarCentangPeserta");
  wadah.innerHTML = "";
  for (const nama of grupAktif.participantNames) {
    const id = "centang-" + nama;
    wadah.innerHTML +=
      `<div class="form-check"><input class="form-check-input" type="checkbox" ` +
      `value="${nama}" id="${id}"><label class="form-check-label" for="${id}">${nama}</label></div>`;
  }
}

function gantiJenisSplit() {
  const jenis = document.getElementById("pilihJenisSplit").value;
  const pakaiShares = jenis !== "EQUAL";
  document.getElementById("areaEqual").classList.toggle("d-none", pakaiShares);
  document.getElementById("areaShares").classList.toggle("d-none", !pakaiShares);
  document.getElementById("labelShares").textContent =
    jenis === "PERCENTAGE"
      ? "Rincian persen per orang (total harus 100)"
      : jenis === "EXACT"
        ? "Rincian nominal per orang (total harus pas)"
        : "Rincian: isi angka = nominal pasti, kosongkan = ikut bagi rata sisa";
}

function siapkanBarisShares() {
  document.getElementById("daftarBarisShares").innerHTML = "";
  for (const nama of grupAktif.participantNames) tambahBarisShare(nama);
}

function tambahBarisShare(namaAwal = "") {
  const wadah = document.getElementById("daftarBarisShares");
  const baris = document.createElement("div");
  baris.className = "input-group input-group-sm mb-2";
  baris.innerHTML =
    `<select class="form-select baris-nama"></select>` +
    `<input type="number" min="0" step="0.01" class="form-control baris-nilai" placeholder="angka (boleh kosong utk sisa)">` +
    `<button class="btn btn-outline-danger" onclick="this.parentElement.remove()">✕</button>`;
  const pilihanNama = baris.querySelector(".baris-nama");
  for (const nama of grupAktif.participantNames) {
    const opsi = document.createElement("option");
    opsi.value = nama;
    opsi.textContent = nama;
    if (nama === namaAwal) opsi.selected = true;
    pilihanNama.appendChild(opsi);
  }
  wadah.appendChild(baris);
}

function bacaBarisShares() {
  const hasil = [];
  for (const baris of document.querySelectorAll("#daftarBarisShares > div")) {
    const nama = baris.querySelector(".baris-nama").value;
    const nilaiTeks = baris.querySelector(".baris-nilai").value;
    hasil.push({ personName: nama, shareValue: nilaiTeks === "" ? null : Number(nilaiTeks) });
  }
  return hasil;
}

async function tambahExpenseBaru() {
  const jenis = document.getElementById("pilihJenisSplit").value;
  const badan = {
    description: document.getElementById("inputDeskripsi").value.trim(),
    amount: Number(document.getElementById("inputNominal").value),
    paidBy: document.getElementById("pilihPembayar").value,
    splitType: jenis,
  };
  if (jenis === "EQUAL") {
    const tercentang = [...document.querySelectorAll("#daftarCentangPeserta input:checked")].map(
      (kotak) => kotak.value,
    );
    if (tercentang.length > 0) badan.splitBetween = tercentang;
  } else {
    badan.shares = bacaBarisShares();
  }
  try {
    await panggilApi(`/api/groups/${grupAktif.groupId}/expenses`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(badan),
    });
    document.getElementById("inputDeskripsi").value = "";
    document.getElementById("inputNominal").value = "";
    tampilkanPesan("success", "Expense tersimpan.");
    muatUlangTampilan();
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

async function muatUlangTampilan() {
  if (!grupAktif) return;
  try {
    const grup = await panggilApi(`/api/groups/${grupAktif.groupId}`);
    grupAktif = grup;
    tampilkanIsiGrup(grup);
    const settlement = await panggilApi(`/api/groups/${grupAktif.groupId}/settlement`);
    tampilkanSettlement(settlement);
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

function ringkasShares(expense) {
  return expense.shares.map((s) => `${s.personName}: ${formatRupiah(s.owedAmount)}`).join("<br>");
}

function tampilkanIsiGrup(grup) {
  document.getElementById("judulGrup").textContent = grup.groupName;
  document.getElementById("judulGrup").classList.remove("text-muted");
  document.getElementById("teksGroupId").textContent = "ID: " + grup.groupId;
  const totalBelanja = grup.expenses.reduce((jumlah, e) => jumlah + Number(e.amount), 0);
  document.getElementById("badgeTotal").textContent =
    `${grup.expenses.length} expense · ${formatRupiah(totalBelanja)}`;

  const badan = document.getElementById("badanTabelExpense");
  if (grup.expenses.length === 0) {
    badan.innerHTML = '<tr><td colspan="4" class="text-muted">Belum ada expense.</td></tr>';
    return;
  }
  badan.innerHTML = "";
  for (const expense of grup.expenses) {
    badan.innerHTML +=
      `<tr><td>${expense.description}</td>` +
      `<td>${formatRupiah(expense.amount)}</td>` +
      `<td>${expense.paidBy}</td>` +
      `<td><span class="badge text-bg-secondary mb-1">${expense.splitType}</span><br>` +
      `<small>${ringkasShares(expense)}</small></td></tr>`;
  }
}

function tampilkanSettlement(settlement) {
  document.getElementById("badgeCharge").textContent =
    `charge ${settlement.service_charge_pct}% (${formatRupiah(settlement.service_charge_amount)})`;

  const badan = document.getElementById("badanTabelSaldo");
  badan.innerHTML = "";
  for (const saldo of settlement.balances) {
    const angka = Number(saldo.netBalance);
    const lencana =
      angka > 0
        ? `<span class="badge text-bg-success">+${formatRupiah(angka)}</span>`
        : angka < 0
          ? `<span class="badge text-bg-danger">${formatRupiah(angka)}</span>`
          : `<span class="badge text-bg-secondary">lunas</span>`;
    badan.innerHTML +=
      `<tr><td>${saldo.personName}</td>` +
      `<td>${formatRupiah(saldo.totalPaid)}</td>` +
      `<td>${formatRupiah(saldo.totalOwed)}</td><td>${lencana}</td></tr>`;
  }

  const daftar = document.getElementById("daftarTransfer");
  if (settlement.transfers.length === 0) {
    daftar.innerHTML = '<li class="list-group-item text-muted">Semua sudah lunas.</li>';
    return;
  }
  daftar.innerHTML = "";
  for (const transfer of settlement.transfers) {
    daftar.innerHTML +=
      `<li class="list-group-item"><b>${transfer.from}</b> → <b>${transfer.to}</b>: ` +
      `${formatRupiah(transfer.amount)}</li>`;
  }
}

muatPilihanGrupTersimpan();
gantiJenisSplit();
