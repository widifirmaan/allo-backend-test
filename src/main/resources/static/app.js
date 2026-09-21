

let grupAktif = null;
let daftarGrupDb = [];
let expenseSedangDiedit = null;

function escapeHtml(teks) {
  return String(teks ?? "")
    .replaceAll("&", "&amp;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;")
    .replaceAll('"', "&quot;")
    .replaceAll("'", "&#39;");
}

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
  if (response.status === 204) {
    return null;
  }
  const badan = await response.json().catch(() => ({}));
  if (!response.ok) {
    throw new Error(badan.message || "Terjadi kesalahan");
  }
  return badan;
}

async function muatPilihanGrupTersimpan(groupIdTerpilih) {
  const pilihan = document.getElementById("pilihGrupTersimpan");
  try {
    const daftar = await panggilApi("/api/groups");
    daftarGrupDb = Array.isArray(daftar) ? daftar : [];
    daftarGrupDb.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    pilihan.innerHTML = '<option value="">— pilih —</option>';
    for (const grup of daftarGrupDb) {
      const opsi = document.createElement("option");
      opsi.value = grup.groupId;
      opsi.textContent = `${grup.groupName} (${String(grup.groupId).slice(0, 8)}…)`;
      pilihan.appendChild(opsi);
    }
    const terpilih = groupIdTerpilih || (grupAktif && grupAktif.groupId);
    if (terpilih) pilihan.value = terpilih;
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

async function buatGrupBaru() {
  const namaGrup = document.getElementById("inputNamaGrup").value.trim();
  const peserta = document
    .getElementById("inputPeserta")
    .value.split(",")
    .map((nama) => nama.trim())
    .filter((nama) => nama.length > 0);
  if (peserta.length < 2) {
    tampilkanPesan("warning", "Peserta minimal 2 orang, pisahkan dengan koma. Contoh: Andi, Budi");
    return;
  }
  try {
    const grup = await panggilApi("/api/groups", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ groupName: namaGrup, participantNames: peserta }),
    });
    await muatPilihanGrupTersimpan(grup.groupId);
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
    await muatPilihanGrupTersimpan(grup.groupId);
    pilihGrupAktif(grup);
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

function pilihGrupAktif(grup) {
  grupAktif = grup;
  expenseSedangDiedit = null;
  document.getElementById("peringatanBelumAdaGrup").classList.add("d-none");
  document.getElementById("formExpense").classList.remove("d-none");
  document.getElementById("tombolUbahGrup").classList.remove("d-none");
  document.getElementById("tombolHapusGrup").classList.remove("d-none");
  document.getElementById("formUbahGrup").classList.add("d-none");
  aturModeFormExpense();
  siapkanPilihanPembayar();
  siapkanCentangPeserta();
  siapkanBarisShares();
  muatUlangTampilan();
}

function aturModeFormExpense() {
  const sedangUbah = expenseSedangDiedit !== null;
  document.getElementById("judulFormExpense").textContent = sedangUbah
    ? `2. Ubah Expense (${expenseSedangDiedit})`
    : "2. Tambah Expense";
  document.getElementById("tombolSimpanExpense").textContent = sedangUbah
    ? "Update Expense"
    : "Simpan Expense";
  document.getElementById("tombolBatalExpense").classList.toggle("d-none", !sedangUbah);
}

function mulaiUbahGrup() {
  if (!grupAktif) return;
  document.getElementById("inputUbahNamaGrup").value = grupAktif.groupName;
  document.getElementById("inputUbahPeserta").value = grupAktif.participantNames.join(", ");
  document.getElementById("formUbahGrup").classList.remove("d-none");
}

function batalUbahGrup() {
  document.getElementById("formUbahGrup").classList.add("d-none");
}

async function simpanUbahGrup() {
  if (!grupAktif) return;
  const namaBaru = document.getElementById("inputUbahNamaGrup").value.trim();
  const pesertaBaru = document
    .getElementById("inputUbahPeserta")
    .value.split(",")
    .map((nama) => nama.trim())
    .filter((nama) => nama.length > 0);
  const badan = {};
  if (namaBaru) badan.groupName = namaBaru;
  if (pesertaBaru.length > 0) {
    if (pesertaBaru.length < 2) {
      tampilkanPesan("warning", "Peserta minimal 2 orang.");
      return;
    }
    badan.participantNames = pesertaBaru;
  }
  if (!badan.groupName && !badan.participantNames) {
    tampilkanPesan("warning", "Isi nama grup atau peserta dulu.");
    return;
  }
  try {
    const grup = await panggilApi(`/api/groups/${grupAktif.groupId}`, {
      method: "PUT",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(badan),
    });
    batalUbahGrup();
    await muatPilihanGrupTersimpan(grup.groupId);
    grupAktif = grup;
    expenseSedangDiedit = null;
    aturModeFormExpense();
    siapkanPilihanPembayar();
    siapkanCentangPeserta();
    siapkanBarisShares();
    muatUlangTampilan();
    tampilkanPesan("success", "Grup diperbarui.");
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

async function hapusGrupAktif() {
  if (!grupAktif) return;
  if (!confirm(`Hapus grup "${grupAktif.groupName}" beserta semua expenses-nya?`)) return;
  try {
    await panggilApi(`/api/groups/${grupAktif.groupId}`, { method: "DELETE" });
    const namaHapus = grupAktif.groupName;
    grupAktif = null;
    expenseSedangDiedit = null;
    document.getElementById("judulGrup").textContent = "Belum ada grup dibuka";
    document.getElementById("judulGrup").classList.add("text-muted");
    document.getElementById("teksGroupId").textContent = "";
    document.getElementById("teksPeserta").textContent = "";
    document.getElementById("badgeTotal").textContent = "";
    document.getElementById("badanTabelExpense").innerHTML =
      '<tr><td colspan="5" class="text-muted">—</td></tr>';
    document.getElementById("formExpense").classList.add("d-none");
    document.getElementById("peringatanBelumAdaGrup").classList.remove("d-none");
    document.getElementById("tombolUbahGrup").classList.add("d-none");
    document.getElementById("tombolHapusGrup").classList.add("d-none");
    document.getElementById("formUbahGrup").classList.add("d-none");
    await muatPilihanGrupTersimpan();
    tampilkanPesan("success", `Grup <b>${escapeHtml(namaHapus)}</b> dihapus.`);
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
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
  if (!grupAktif) return;
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
  const sedangUbah = expenseSedangDiedit !== null;
  const url = sedangUbah
    ? `/api/groups/${grupAktif.groupId}/expenses/${expenseSedangDiedit}`
    : `/api/groups/${grupAktif.groupId}/expenses`;
  try {
    await panggilApi(url, {
      method: sedangUbah ? "PUT" : "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(badan),
    });
    document.getElementById("inputDeskripsi").value = "";
    document.getElementById("inputNominal").value = "";
    expenseSedangDiedit = null;
    aturModeFormExpense();
    tampilkanPesan("success", sedangUbah ? "Expense diperbarui." : "Expense tersimpan.");
    muatUlangTampilan();
  } catch (kesalahan) {
    tampilkanPesan("danger", kesalahan.message);
  }
}

function mulaiUbahExpense(expenseId) {
  if (!grupAktif) return;
  const expense = grupAktif.expenses.find((e) => e.expenseId === expenseId);
  if (!expense) return;
  expenseSedangDiedit = expenseId;
  document.getElementById("inputDeskripsi").value = expense.description;
  document.getElementById("inputNominal").value = expense.amount;
  document.getElementById("pilihJenisSplit").value = expense.splitType;
  gantiJenisSplit();
  siapkanPilihanPembayar();
  document.getElementById("pilihPembayar").value = expense.paidBy;
  if (expense.splitType === "EQUAL") {
    siapkanCentangPeserta();
    const penanggung = expense.shares.map((s) => s.personName);
    const semuaIkut = grupAktif.participantNames.every((nama) => penanggung.includes(nama)) &&
      penanggung.length === grupAktif.participantNames.length;
    if (!semuaIkut) {
      for (const kotak of document.querySelectorAll("#daftarCentangPeserta input")) {
        kotak.checked = penanggung.includes(kotak.value);
      }
    }
  } else {
    document.getElementById("daftarBarisShares").innerHTML = "";
    for (const share of expense.shares) {
      // PERCENTAGE disimpan sebagai rupiah di response, konversi balik ke persen agar bisa diedit.
      let nilai = share.owedAmount;
      if (expense.splitType === "PERCENTAGE" && Number(expense.amount) > 0) {
        nilai = Math.round((Number(share.owedAmount) / Number(expense.amount)) * 10000) / 100;
      }
      tambahBarisShare(share.personName);
      const barisTerakhir = document.querySelector("#daftarBarisShares > div:last-child .baris-nilai");
      if (barisTerakhir) barisTerakhir.value = nilai;
    }
  }
  aturModeFormExpense();
  document.getElementById("formExpense").scrollIntoView({ behavior: "smooth", block: "start" });
}

function batalUbahExpense() {
  expenseSedangDiedit = null;
  document.getElementById("inputDeskripsi").value = "";
  document.getElementById("inputNominal").value = "";
  aturModeFormExpense();
}

async function hapusExpense(expenseId, deskripsi) {
  if (!grupAktif) return;
  if (!confirm(`Hapus expense "${deskripsi || expenseId}"?`)) return;
  try {
    await panggilApi(`/api/groups/${grupAktif.groupId}/expenses/${expenseId}`, {
      method: "DELETE",
    });
    if (expenseSedangDiedit === expenseId) {
      expenseSedangDiedit = null;
      aturModeFormExpense();
    }
    tampilkanPesan("success", "Expense dihapus.");
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
  return expense.shares.map((s) => `${escapeHtml(s.personName)}: ${formatRupiah(s.owedAmount)}`).join("<br>");
}

function tampilkanIsiGrup(grup) {
  document.getElementById("judulGrup").textContent = grup.groupName;
  document.getElementById("judulGrup").classList.remove("text-muted");
  document.getElementById("teksGroupId").textContent = "ID: " + grup.groupId;
  document.getElementById("teksPeserta").textContent =
    "Peserta: " + grup.participantNames.join(", ");
  const totalBelanja = grup.expenses.reduce((jumlah, e) => jumlah + Number(e.amount), 0);
  document.getElementById("badgeTotal").textContent =
    `${grup.expenses.length} expense · ${formatRupiah(totalBelanja)}`;

  const badan = document.getElementById("badanTabelExpense");
  if (grup.expenses.length === 0) {
    badan.innerHTML = '<tr><td colspan="5" class="text-muted">Belum ada expense.</td></tr>';
    return;
  }
  badan.innerHTML = "";
  for (const expense of grup.expenses) {
    const idAman = escapeHtml(expense.expenseId);
    const deskripsiAman = escapeHtml(expense.description);
    badan.innerHTML +=
      `<tr><td>${deskripsiAman}</td>` +
      `<td>${formatRupiah(expense.amount)}</td>` +
      `<td>${escapeHtml(expense.paidBy)}</td>` +
      `<td><span class="badge text-bg-secondary mb-1">${escapeHtml(expense.splitType)}</span><br>` +
      `<small>${ringkasShares(expense)}</small></td>` +
      `<td class="text-nowrap">` +
      `<button class="btn btn-sm btn-outline-primary me-1" onclick="mulaiUbahExpense('${idAman}')">Ubah</button>` +
      `<button class="btn btn-sm btn-outline-danger" onclick="hapusExpense('${idAman}', '${deskripsiAman}')">Hapus</button>` +
      `</td></tr>`;
  }
}

function tampilkanSettlement(settlement) {
  document.getElementById("badgeCharge").textContent =
    `charge ${settlement.service_charge_pct}% (${formatRupiah(settlement.service_charge_amount)})`;

  const badan = document.getElementById("badanTabelSaldo");
  badan.innerHTML = "";
  const chargePerOrang = {};
  for (const charge of settlement.service_charge_shares || []) {
    chargePerOrang[charge.personName] = charge.chargeAmount;
  }
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
      `<td>${formatRupiah(saldo.totalOwed)}</td>` +
      `<td>${formatRupiah(chargePerOrang[saldo.personName] || 0)}</td><td>${lencana}</td></tr>`;
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
