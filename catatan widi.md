# Tutorial: API Simple Split Bill

> Paduan penjelasan pembuatan api simple split bill

## Bagian 0 — Siapkan Alat

Apps yang dibutuhkan: **Java 21**, **Maven**, **Docker**, **MongoDB**.

**CachyOS / Arch Linux** (karena saya menggunakan cachyos):

```bash
sudo pacman -Sy --noconfirm jdk21-openjdk maven
sudo pacman -Sy --noconfirm docker docker-compose
sudo systemctl enable --now docker
paru -Sy mongodb-bin mongosh-bin
sudo systemctl enable --now mongodb
```

## Bagian 1 — Kerangka Project + `pom.xml`

Saya Memulai dengan **https://start.spring.io** dengan pilih Maven + Java 21, dependensi `Spring Web` + `Validation`.
Hasil `pom.xml`:

```xml
<parent>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-parent</artifactId>
  <version>3.2.5</version>
</parent>

<properties>
  <java.version>21</java.version>
</properties>

<dependencies>
  <!-- Wajib: web (REST) + validation (@NotBlank dkk) -->
  <dependency>...spring-boot-starter-web...</dependency>
  <dependency>...spring-boot-starter-validation...</dependency>

  <!-- Database: driver MongoDB -->
  <dependency>...spring-boot-starter-data-mongodb...</dependency>

  <!-- Dokumentasi API otomatis -->
  <dependency>
    <groupId>org.springdoc</groupId>
    <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
    <version>2.5.0</version>
  </dependency>

  <!-- Test: JUnit + Mockito + MockMvc (satu paket) -->
  <dependency>...spring-boot-starter-test... <scope>test</scope></dependency>
</dependencies>
```

Tambahan di `<build>`: plugin Spring Boot (membuat file jar yang bisa
dijalankan) + plugin **JaCoCo** (pengukur coverage test, dibahas di Bagian 11).

File Java pertama hanya kelas pembuka aplikasi:

```java
@SpringBootApplication
public class SplitBillApplication {
  public static void main(String[] args) {
    SpringApplication.run(SplitBillApplication.class, args);
  }
}
```

Jalankan `mvn test`

---

## Bagian 2 — Struktur Folder

| Folder | Isi |
|---|---|---|
| `controller/` | Terima request HTTP, kembalikan response JSON |
| `service/` | Seluruh logika |
| `service/split/` | Satu class per cara pembagian bikknya |
| `repository/` | Simpan dan ambil data |
| `model/` | Model data |
| `enums/` | Kumpulan enum |
| `dto/` | Bentuk JSON request dan response |
| `exception/` | Error + handler terpusat |
| `config/` | Bean konfigurasi (Swagger, format desimal dll) |
| `seed/` | Pengisi data demo |
| `resources/static/` | File Web UI yang disajikan langsung |

---

## Bagian 3 — Model dan Uang

**`Expense`**

```java
public record Expense(
    String expenseId,
    String description,
    BigDecimal amount, // <-- WAJIB BigDecimal
    String paidBy,
    SplitType splitType,
    Map<String, BigDecimal> utangPerOrang, 
    Instant createdAt) {}
```

**`BillGroup`** — grup berisi nama, peserta, dan daftar expense:

```java
public class BillGroup {

  private final String groupId;
  private final String groupName;
  private final List<String> participantNames;
  private final List<Expense> expenseList = new ArrayList<>();
  private final Instant createdAt;
}
```

---

## Bagian 4 — Enum

**`SplitType`** (folder `enums/`) — enum 4 cara bagi:

```java
public enum SplitType {
  EQUAL,                    // bagi rata
  PERCENTAGE,               // per persen, total harus 100
  EXACT,                    // nominal pasti, total harus pas
  EXACT_REMAINDER_EQUAL     // sebagian exact, sisanya bagi rata
}
```

---

## Bagian 5 — DTO + Validasi

Isi folder `dto/` (7 file):

**JSON masuk:**

- `CreateGroupRequest` > `POST /api/groups`. Isi `groupName`, `participantNames` (+ validasi `@NotBlank`, `@NotEmpty`).
- `AddExpenseRequest` > `POST /api/groups/{groupId}/expenses`. Isi `description`, `amount`, `paidBy`, `splitType`, `splitBetween`, `shares` (+ `@DecimalMin("0.01")` untuk nominal).
- `ShareInput` > dipakai di dalam `AddExpenseRequest` untuk `shares`. Satu baris `personName` + `shareValue`. Tanpa `@NotNull` karena untuk split campuran null itu sah ("ikut bagi rata sisa").

**JSON keluar:**

- `GroupResponse` > `POST /api/groups`, `GET /api/groups/{groupId}`. Isi grup + daftar `expenses` (pakai ulang `ExpenseResponse`).
- `ExpenseResponse` > `POST /api/groups/{groupId}/expenses` (dan di dalam `GroupResponse`). Isi satu expense + rincian `shares` per orang.
- `SettlementResponse` > `GET /api/groups/{groupId}/settlement`. Isi `balances`, `transfers`, `service_charge_pct`, `service_charge_amount`
- `ErrorResponse` > semua endpoint saat error. Format `{error, message}`.

---

## Bagian 6 — Penyimpanan MongoDB

Service tidak akses database langsung, melainkan lewat interface:

```java
public interface BillGroupStore {
  BillGroup saveNewGroup(String groupName, List<String> participantNames);
  BillGroup save(BillGroup grup);  
  Optional<BillGroup> findById(String groupId);
  default BillGroup findByIdOrThrow(String groupId) { ... }
}
```

---

## Bagian 7 — Service: Aturan Bisnis, Strategi Split, Settlement

Isi folder `service/` (10 file):

- `BillGroupService.java` > buat grup + ambil grup. Cek nama peserta (tidak kosong, tidak duplikat).
- `ExpenseService.java` > tambah expense. Cek `paidBy` peserta, cek `amount` > 0, pilih cara pembagian billnya > simpan db.
- `SettlementService.java` > hitung saldo + transfer minimal.
- `ServiceChargeCalculator.java` > rumus charge + fungsi pembulatan uang.
- `split/SplitStrategy.java` > aturan pembagian uang agar tidak ada sisa jika (10 dibagi 3).
- `split/EqualSplitStrategy.java` > bagi rata.
- `split/PercentageSplitStrategy.java` > bagi persen (total harus 100).
- `split/ExactSplitStrategy.java` > nominal pasti (jumlah harus pas).
- `split/ExactRemainderEqualStrategy.java` > sebagian exact, sisa bagi rata.
- `split/SplitStrategyFactory.java` > cara pembagian yang dipilih `SplitType`.

---

## Bagian 8 — Controller + Error Rapi + Swagger

Isi folder `controller/` (3 file):

- `BillGroupController.java` > `POST /api/groups`, `GET /api/groups/{groupId}`
- `ExpenseController.java` > `POST /api/groups/{groupId}/expenses`
- `SettlementController.java` > `GET /api/groups/{groupId}/settlement`

Controller: terima JSON, panggil service, kembalikan JSON.

Error satu pintu (`GlobalExceptionHandler`), format `{error, message}`:

- Grup tidak ada > 404 `GROUP_NOT_FOUND`
- Aturan bisnis dilanggar > 400 `INVALID_REQUEST`. Contoh: `paidBy` bukan
  peserta, total persen tidak 100, jumlah EXACT tidak pas = amount, jumlah
  exact ≥ total di split campuran, `shares` kosong, nama duplikat.
- Validasi gagal > 400 `VALIDATION_FAILED`. Contoh: nama kosong, amount
  negatif/null. Ini dicek anotasi sebelum masuk service.

**Swagger UI** (http://localhost:4110/swagger-ui.html): `@Tag` +
`@Operation` per endpoint, `@Schema(example=...)` per field, dropdown
`@ExampleObject` berisi 5 contoh siap eksekusi.

---

## Bagian 9 — Konfigurasi + Seeder

Isi `application.properties`:

```properties
server.port=4110
app.github-username=widifirmaan
spring.data.mongodb.uri=mongodb://localhost:27017/splitbill
app.seed=false                                  # true = isi data demo
```

**Seeder** (`DemoDataSeeder`, aktif hanya bila `app.seed=true`): mengisi grup
`demo-trip-bandung` + 4 expense (satu per jenis split). ID tetap dan dicek
dulu, jadi restart tidak bikin data ganda. Bangun objek model langsung
karena service selalu bikin UUID baru.

---

## Bagian 10 — Web UI

File di `resources/static/` otomatis disajikan Spring Boot. Isinya:

- `index.html` — layout Bootstrap 5.3.3 (2 kolom di laptop, 1 kolom di HP).
- `app.js` — `fetch()` ke REST API. Pola: `panggilApi()` →
  `tampilkanIsiGrup()` + `tampilkanSettlement()`. Setiap aksi diakhiri
  muat-ulang.
- Bootstrap disimpan lokal (`static/vendor/`), halaman jalan full offline.
- Daftar grup tersimpan di `localStorage` browser.

---

## Bagian 11 — Unit Test + Coverage (47 test)

Test untuk tiap aturan yang melempar error:

| Test | Yang dipastikan |
|---|---|
| `SettlementServiceTest` | Hitungan manual + semua jenis split + penolakan persen/exact salah |
| `SplitStrategyTest` | Tiap strategi + total selalu pas (sen tak hilang) |
| `ExpenseServiceTest` | 10 aturan validasi (pembayar, nominal, duplikat, konflik field…) |
| `BillGroupServiceTest` | Validasi nama + GET grup tampil semua expense |
| `ServiceChargeCalculatorTest` | Rumus 3%, case-insensitive, charge 0% |
| `BillGroupDocumentTest` | Round-trip model↔dokumen |
| `*ControllerTest` (MockMvc) | Status 201/400/404 + format error + field charge |
| `DemoDataSeederTest` (Mockito) | Seed saat kosong, skip saat sudah ada |
| `SplitStrategyFactoryTest` | Mapping jenis→implementasi |

Test factory pernah menemukan bug: `Map.get(null)` melempar NPE,
saya perbaiki jadi `IllegalArgumentException`.

```bash
./mvnw test
# Laporan: target/site/jacoco/index.html  (instruksi ~90%, branch ~88%)
```

---

## Bagian 12 — Menjalankan

1. **Lokal** (MongoDB native sudah jalan dari Bagian 0):

```bash
./mvnw package
java -jar target/app.jar
```

2. **Docker Compose** (app + MongoDB permanen):

```bash
docker compose up --build -d
docker compose down      # berhenti, data tetap (volume data-mongo)
docker compose down -v   # berhenti + hapus data
```

3. **Data demo**:
  Lokal > `APP_SEED=true java -jar target/app.jar`.
  Docker > ganti `APP_SEED` jadi `"true"` di `docker-compose.yml`, lalu `docker compose up --build -d`.

4. **ICek DB langsung**:

```bash
docker exec -it splitbill-mongo mongosh
> use splitbill
> db.bill_groups.findOne()
```

Buka setelah jalan:

- Web UI > http://localhost:4110/
- Swagger > http://localhost:4110/swagger-ui.html
