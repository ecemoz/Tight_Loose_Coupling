# Tight vs Loose Coupling ve Spring IoC: Adım Adım Öğrenme Rehberi

Bu proje, **bağımlılık yönetimi** konusunu sıfırdan öğrenmen için hazırlandı. Her bölüm bir öncekinin üstüne eklenir. Bu yüzden bölümleri **sırayla** okuman ve ilgili paketteki kodu açıp yanında takip etmen en verimlisi olur.

---

## İçindekiler

1. [Bu rehberin sonunda neleri öğrenmiş olacaksın?](#1-bu-rehberin-sonunda-neleri-öğrenmiş-olacaksın)
2. [Önce temel kavram: Bağımlılık nedir?](#2-önce-temel-kavram-bağımlılık-nedir)
3. [Adım 1: Tight Coupling (Sıkı Bağlılık)](#3-adım-1-tight-coupling-sıkı-bağlılık)
4. [Adım 2: Loose Coupling (Gevşek Bağlılık)](#4-adım-2-loose-coupling-gevşek-bağlılık)
5. [Adım 3: Spring IoC ve Bean Kavramı](#5-adım-3-spring-ioc-ve-bean-kavramı)
6. [Adım 4: Constructor Injection](#6-adım-4-constructor-injection)
7. [Adım 5: Setter Injection](#7-adım-5-setter-injection)
8. [Adım 6: Autowire (Otomatik Bağlama)](#8-adım-6-autowire-otomatik-bağlama)
9. [Hepsini karşılaştıralım](#9-hepsini-karşılaştıralım)
10. [Proje yapısı](#10-proje-yapısı)
11. [Projeyi çalıştırma](#11-projeyi-çalıştırma)
12. [Sık yapılan hatalar](#12-sık-yapılan-hatalar)
13. [Kendini test et](#13-kendini-test-et)
14. [Son özet](#14-son-özet)

---

## 1) Bu rehberin sonunda neleri öğrenmiş olacaksın?

Rehberi bitirdiğinde şu soruları kendi cümlelerinle cevaplayabileceksin:

- Neden **tight coupling** (sıkı bağlılık) bir problemdir?
- **Loose coupling** (gevşek bağlılık) nasıl kurulur?
- **Spring IoC container** nesneleri nasıl yaratır ve yönetir?
- **Constructor injection**, **setter injection** ve **autowire** nedir, hangisi ne zaman seçilir?

**Ön bilgi:** Temel Java (sınıf, nesne, interface) bilmen yeterli. Spring bilmen gerekmiyor, onu da burada öğreneceksin.

---

## 2) Önce temel kavram: Bağımlılık nedir?

Bir sınıf, işini yapabilmek için başka bir sınıfa ihtiyaç duyuyorsa, o sınıfa **bağımlıdır**.

> **Günlük hayattan örnek:** Bir araba (Car), çalışabilmek için bir motora (Engine) ihtiyaç duyar.
> Araba motora **bağımlıdır**. Motor ise arabanın **bağımlılığıdır** (dependency).

Bağımlılık olması kötü bir şey değildir, hatta kaçınılmazdır. Asıl soru şudur:

> **Araba, motorunu kim yaratıyor?**
> - Araba kendi motorunu kendi üretiyorsa → **sıkı bağlılık**
> - Motor dışarıdan arabaya takılıyorsa → **gevşek bağlılık**

Bu rehberin tamamı bu soruya cevap arar.

---

## 3) Adım 1: Tight Coupling (Sıkı Bağlılık)

📦 Paket: `com.tight.coupling`

### Kod ne yapıyor?

`UserManager` sınıfı, ihtiyaç duyduğu `UserDatabase` nesnesini **kendisi** yaratıyor:

```java
public class UserManager {

    // UserManager, bağımlılığını kendisi yaratıyor 👇
    private UserDatabase userDatabase = new UserDatabase();

    public void getUserData() {
        userDatabase.getData();
    }
}
```

Buradaki kritik satır `new UserDatabase()` satırıdır. `UserManager`, **hangi** veri kaynağını kullanacağına kendisi karar vermiş ve bunu koda gömmüş durumda.

### Sorun ne?

Bir düşün: Bir gün veriyi veritabanından değil, bir **web servisinden** almak istiyorsun. Ne yapman gerekir?

```java
// Eski hali
private UserDatabase userDatabase = new UserDatabase();

// Yeni hali: UserManager sınıfının İÇİNİ değiştirmek zorundasın
private WebService webService = new WebService();
```

Bu durumda şu problemler ortaya çıkar:

| Problem | Açıklama |
|---|---|
| ❌ **Değiştirmesi zor** | Veri kaynağını değiştirmek için `UserManager`'ın kodunu açıp düzenlemek gerekir. |
| ❌ **Test etmesi zor** | Test sırasında gerçek veritabanı yerine sahte (mock) bir kaynak veremezsin. Her testte gerçek veritabanı devreye girer. |
| ❌ **Esnek değil** | Birden fazla veri kaynağını desteklemek için sınıfı sürekli büyütmen gerekir. |

### Altın kural

> **Bir sınıf kendi bağımlılığını kendisi yaratıyorsa (`new` ile), tight coupling vardır.**

---

## 4) Adım 2: Loose Coupling (Gevşek Bağlılık)

📦 Paket: `com.loose.coupling`

### Çözümün fikri

Sıkı bağlılıktan kurtulmak için iki şey yapıyoruz:

1. **Somut sınıfa değil, soyut bir sözleşmeye (interface) bağlanıyoruz.**
2. **Bağımlılığı sınıfın içinde yaratmak yerine, dışarıdan veriyoruz.**

### Kullanılan parçalar

| Sınıf / Interface | Görevi |
|---|---|
| `UserDataProvider` | **Sözleşme.** "Veri sağlayan her şey şu metodu sunmalı" der. |
| `UserDatabaseProvider` | Sözleşmenin **veritabanı** ile yapılan gerçekleştirmesi |
| `WebServiceDataProvider` | Sözleşmenin **web servisi** ile yapılan gerçekleştirmesi |
| `UserManager` | Artık somut sınıfa değil, **interface'e** bağlı |

### Kodla görelim

**Sözleşme (interface):**

```java
public interface UserDataProvider {
    void getData();
}
```

**İki farklı gerçekleştirme:**

```java
public class UserDatabaseProvider implements UserDataProvider {
    @Override
    public void getData() {
        System.out.println("Veri VERİTABANINDAN alındı");
    }
}

public class WebServiceDataProvider implements UserDataProvider {
    @Override
    public void getData() {
        System.out.println("Veri WEB SERVİSİNDEN alındı");
    }
}
```

**Artık esnek olan UserManager:**

```java
public class UserManager {

    private UserDataProvider userDataProvider;

    // Bağımlılık DIŞARIDAN veriliyor 👇
    public UserManager(UserDataProvider userDataProvider) {
        this.userDataProvider = userDataProvider;
    }

    public void getUserData() {
        userDataProvider.getData();
    }
}
```

**Kullanım:**

```java
// Veritabanı ile çalıştır
UserManager manager1 = new UserManager(new UserDatabaseProvider());
manager1.getUserData();   // Veri VERİTABANINDAN alındı

// Aynı UserManager'ı, KODUNA DOKUNMADAN web servisi ile çalıştır
UserManager manager2 = new UserManager(new WebServiceDataProvider());
manager2.getUserData();   // Veri WEB SERVİSİNDEN alındı
```

### Neden daha iyi?

`UserManager` artık **verinin nereden geldiğini bilmiyor** ve bilmesine de gerek yok. Tek bildiği şey: "Bana `UserDataProvider` sözleşmesine uyan biri veriliyor, ben ondan `getData()` isteyebilirim."

| Kazanım | Açıklama |
|---|---|
| ✅ Yeni kaynak eklemek kolay | Yeni bir sınıf yaz, `UserDataProvider`'ı uygulasın. `UserManager`'a dokunma. |
| ✅ Test etmek kolay | Test sırasında sahte bir provider verebilirsin. |
| ✅ Kod esnek | Çalışma zamanında hangi kaynağın kullanılacağı değiştirilebilir. |

### 🔑 Buradaki temel fikir: Dependency Injection (DI)

```java
public UserManager(UserDataProvider userDataProvider)
```

Bu satır **Dependency Injection** (bağımlılık enjeksiyonu) mantığını gösterir:

> **Bağımlılık, sınıfın içinde yaratılmaz. Dışarıdan verilir (enjekte edilir).**

### Peki bu nesneleri dışarıdan kim verecek?

Yukarıdaki örnekte `new UserDatabaseProvider()` yazıp bağımlılığı **biz elle** verdik. Büyük projelerde yüzlerce sınıf olduğunu düşün: hepsini elle birbirine bağlamak çok zahmetli olur.

İşte **Spring** tam bu noktada devreye giriyor. 👇

---

## 5) Adım 3: Spring IoC ve Bean Kavramı

📦 Paket: `car.example.bean`

### IoC nedir?

**IoC = Inversion of Control (Kontrolün Tersine Çevrilmesi)**

| | Kim nesneyi yaratır ve yönetir? |
|---|---|
| Klasik yöntem | **Sen**, `new` ile |
| IoC (Spring) | **Spring container** |

> **Restoran benzetmesi:**
> Klasik yöntemde malzemeleri alır, yemeği kendin pişirirsin.
> IoC'de ise garsona siparişi verirsin, yemeği mutfak (Spring container) hazırlar ve masana getirir.
> Kontrol sende değil, mutfaktadır. Yani kontrol **tersine çevrilmiştir**.

### Bean nedir?

**Bean**, Spring container tarafından yaratılan ve yönetilen nesnedir. Sıradan bir Java nesnesidir, farkı onu Spring'in yönetmesidir.

### Bu pakette hangi dosyalar var?

| Dosya | Görevi |
|---|---|
| `MyBean` | Basit bir bean sınıfı |
| `App` | Spring context'ten bean'i alan ana sınıf |
| `applicationBeanContext.xml` | Bean'in tanımlandığı yapılandırma dosyası |

### XML ile bean tanımı

```xml
<bean id="myBean" class="car.example.bean.MyBean"/>
```

Bu satır Spring'e şunu söyler: *"`myBean` adında bir nesne olsun, `MyBean` sınıfından yaratılsın."*

### Java tarafında bean'i almak

```java
// 1) Spring container'ı başlat (XML dosyasını okur, bean'leri yaratır)
ApplicationContext applicationContext =
        new ClassPathXmlApplicationContext("applicationBeanContext.xml");

// 2) Container'dan nesneyi iste
MyBean myBean = applicationContext.getBean("myBean", MyBean.class);
```

### Burada ne öğrendik?

Dikkat et: `MyBean myBean = new MyBean();` **yazmadık**. Nesne `new` ile değil, **container aracılığıyla** geldi.

```
┌─────────────────────────────────────┐
│        Spring Container (IoC)       │
│                                     │
│   XML'i okur → Bean'leri yaratır    │
│              → Yönetir              │
│              → İstenince verir      │
└──────────────┬──────────────────────┘
               │ getBean("myBean")
               ▼
          Senin kodun
```

---

## 6) Adım 4: Constructor Injection

📦 Paket: `car.example.constructor.injection`

### Fikir

Bağımlılık, nesne yaratılırken **constructor (yapıcı metot) üzerinden** verilir.

### Mantık

- `Car` sınıfı, `Specification` (araç özellikleri) olmadan **eksiktir**.
- Bu yüzden `Specification`, constructor üzerinden **zorunlu** olarak verilir.
- `Specification` verilmeden `Car` nesnesi yaratılamaz.

### Java tarafı

```java
public class Car {

    private Specification specification;

    // Specification vermeden Car yaratamazsın 👇
    public Car(Specification specification) {
        this.specification = specification;
    }
}
```

### XML tarafı

📄 `applicationConstructionInjection.xml`

```xml
<!-- Önce bağımlılık olan bean -->
<bean id="carSpecification" class="car.example.constructor.injection.Specification"/>

<!-- Sonra onu constructor'a veren bean -->
<bean id="myCar" class="car.example.constructor.injection.Car">
    <constructor-arg ref="carSpecification"/>
</bean>
```

Buradaki `<constructor-arg ref="carSpecification"/>` satırı şunu der:

> *"`myCar` yaratılırken, constructor'a `carSpecification` bean'ini ver."*

`ref` = reference (başka bir bean'e referans) demektir.

### Ne zaman tercih edilir?

- ✅ Bağımlılık **zorunluysa**
- ✅ Nesne, bağımlılık olmadan **anlamlı değilse**

> **Benzetme:** Motorsuz araba olmaz. Motoru üretim sırasında takarsın, sonradan değil.

---

## 7) Adım 5: Setter Injection

📦 Paket: `car.example.setter.injection`

### Fikir

Bağımlılık, nesne yaratıldıktan **sonra**, bir **setter metodu** ile verilir.

### Mantık

`Car` önce boş olarak yaratılır, sonra `setSpecification(...)` ile tamamlanır.

### Java tarafı

```java
public class Car {

    private Specification specification;

    // Constructor'da bağımlılık YOK, nesne boş yaratılabilir
    public Car() {
    }

    // Bağımlılık sonradan setter ile verilir 👇
    public void setSpecification(Specification specification) {
        this.specification = specification;
    }
}
```

### XML tarafı

📄 `applicationSetterInjection.xml`

```xml
<bean id="carSpecification" class="car.example.setter.injection.Specification"/>

<bean id="myCar" class="car.example.setter.injection.Car">
    <property name="specification" ref="carSpecification"/>
</bean>
```

`<property name="specification" ...>` ifadesi Spring'e şunu söyler:

> *"`setSpecification(...)` metodunu çağır ve `carSpecification` bean'ini ver."*

⚠️ **Dikkat:** `name="specification"` değeri, setter adından türetilir: `setSpecification` → `specification`.

### Ne zaman tercih edilir?

- ✅ Bağımlılık **opsiyonelse** (olmasa da nesne çalışabiliyorsa)
- ✅ Bağımlılık **sonradan değiştirilebilir** olmalıysa

> **Benzetme:** Arabaya sonradan takılan portbagaj veya multimedya ekranı gibi. Olmasa da araba çalışır, istediğin zaman değiştirebilirsin.

---

## 8) Adım 6: Autowire (Otomatik Bağlama)

### Sorun

Şimdiye kadar bean'leri birbirine `ref` ile **elle** bağladık. Bean sayısı arttıkça bu çok uzun bir XML'e dönüşür.

### Çözüm

**Autowire**, Spring'in bean'leri sen söylemeden, **kendi başına eşleştirmesidir**.

Bu projede 3 farklı autowire yolu var:

---

### a) `byName` (isme göre)

📦 `car.example.autowire.name` · 📄 `autowireByName.xml`

Spring, **setter adı ile bean `id`'sini** eşleştirir.

```xml
<bean id="specification" class="car.example.autowire.name.Specification"/>

<bean id="myCar" class="car.example.autowire.name.Car" autowire="byName"/>
```

**Eşleşme nasıl olur?**

| Car sınıfındaki setter | Aranan bean id |
|---|---|
| `setSpecification(...)` | `specification` |

Spring, `Car` içinde `setSpecification` görür, "`specification` adında bir bean var mı?" diye bakar ve bulursa otomatik bağlar.

⚠️ **Kritik nokta:** Bean `id`'si, setter adıyla uyuşmalıdır. `id="carSpecification"` yazarsan eşleşme **olmaz**.

---

### b) `byType` (türe göre)

📦 `car.example.autowire.type` · 📄 `autowireByType.xml`

Spring, bağımlılığın **sınıf türüne (type)** bakarak eşleştirir. İsmin önemi yoktur.

```xml
<bean id="anyNameYouWant" class="car.example.autowire.type.Specification"/>

<bean id="myCar" class="car.example.autowire.type.Car" autowire="byType"/>
```

Spring şöyle düşünür: *"`Car`, `Specification` türünde bir bağımlılık istiyor. Container'da `Specification` türünde bir bean var mı? Evet, onu bağlayayım."*

⚠️ **Kritik nokta:** Container'da aynı türden **birden fazla** bean varsa Spring hangisini seçeceğini bilemez ve hata verir.

---

### c) `constructor` (constructor'a göre)

📦 `car.example.autowire.constructor` · 📄 `autowireByConstructor.xml`

Spring, **constructor parametresinin türüne** bakarak bağımlılığı bulur ve constructor üzerinden enjekte eder.

```xml
<bean id="specification" class="car.example.autowire.constructor.Specification"/>

<bean id="myCar" class="car.example.autowire.constructor.Car" autowire="constructor"/>
```

`<constructor-arg>` yazmana gerek kalmaz; Spring constructor'ın parametre türüne bakıp uygun bean'i kendisi verir.

---

### Autowire türleri bir bakışta

| Tür | Neye bakar? | Nasıl enjekte eder? | Dikkat edilecek nokta |
|---|---|---|---|
| `byName` | Setter adı = bean id | Setter | İsimler birebir aynı olmalı |
| `byType` | Bağımlılığın türü | Setter | Aynı türden birden fazla bean olmamalı |
| `constructor` | Constructor parametre türü | Constructor | Tür benzersiz olmalı |

---

## 9) Hepsini karşılaştıralım

### Hangi enjeksiyon türü ne zaman?

| Özellik | Constructor Injection | Setter Injection |
|---|---|---|
| Bağımlılık ne zaman verilir? | Nesne yaratılırken | Nesne yaratıldıktan sonra |
| Bağımlılık zorunlu mu? | ✅ Evet | ❌ Opsiyonel |
| Sonradan değiştirilebilir mi? | Genelde hayır | Evet |
| XML etiketi | `<constructor-arg>` | `<property>` |
| Ne zaman seç? | Eksik olunca nesne anlamsızsa | Olmasa da nesne çalışıyorsa |

### Öğrenme yolculuğu özeti

```
Tight Coupling      →  Sorun: sınıf bağımlılığını kendisi yaratıyor (new)
      ↓
Loose Coupling      →  Çözüm: interface + bağımlılığı dışarıdan ver (DI)
      ↓
Spring IoC          →  Nesneleri senin yerine Spring yaratıp yönetiyor
      ↓
Constructor / Setter→  Bağımlılığı HANGİ YOLLA vereceğiz?
      ↓
Autowire            →  Bağlamayı elle yapmayalım, Spring yapsın
```

---

## 10) Proje yapısı

### Paketler

| Paket | Amaç |
|---|---|
| `com.tight.coupling` | Doğrudan bağımlı (kötü) tasarım örneği |
| `com.loose.coupling` | Interface ile gevşek bağlılık |
| `car.example.bean` | Spring bean ve XML yapılandırması |
| `car.example.constructor.injection` | Constructor injection |
| `car.example.setter.injection` | Setter injection |
| `car.example.autowire.name` | Autowire byName |
| `car.example.autowire.type` | Autowire byType |
| `car.example.autowire.constructor` | Autowire constructor |

### XML dosyaları

| Dosya | Ne anlatıyor? |
|---|---|
| `applicationBeanContext.xml` | Basit bean tanımı |
| `applicationConstructionInjection.xml` | Constructor injection |
| `applicationSetterInjection.xml` | Setter injection |
| `applicationIoCLooseCouplingExample.xml` | Loose coupling + Spring IoC birlikte |
| `autowireByName.xml` | Autowire by name |
| `autowireByType.xml` | Autowire by type |
| `autowireByConstructor.xml` | Autowire by constructor |

### Önerilen okuma sırası

1. `com.tight.coupling`
2. `com.loose.coupling`
3. `car.example.bean`
4. `car.example.constructor.injection`
5. `car.example.setter.injection`
6. `car.example.autowire.name`
7. `car.example.autowire.type`
8. `car.example.autowire.constructor`

> 💡 **İpucu:** `applicationIoCLooseCouplingExample.xml` dosyası, 2. adımdaki (loose coupling) kodun Spring ile nasıl yönetildiğini gösterir. Spring bölümlerini bitirdikten sonra bu dosyaya dönüp bakmak, öğrendiklerini birleştirmeni sağlar.

---

## 11) Projeyi çalıştırma

### Gereksinimler

- **Java 21**
- **Maven**

### Çalıştırma

Proje Maven ile yazılmıştır ve Java 21 hedefler. Spring örneklerini çalıştırırken şu iki yoldan birini kullanmalısın:

1. **IDE üzerinden:** IntelliJ IDEA / Eclipse ile projeyi Maven projesi olarak aç ve `main` metodu olan sınıfı (örneğin `App`) çalıştır.
2. **Maven classpath'i ile:** Bağımlılıkların classpath'e eklenmesini sağlayacak şekilde Maven üzerinden çalıştır.

### ⚠️ Bilinen hata: `NoClassDefFoundError`

Spring örneklerini sadece `javac` / `java` ile veya Maven bağımlılıkları olmadan çalıştırırsan şu hatayı görebilirsin:

```
NoClassDefFoundError: org/springframework/...
```

**Sebebi:** Spring kütüphaneleri classpath'te bulunamıyor.
**Çözümü:** Projeyi Maven üzerinden yükle ve IDE'nin Maven bağımlılıklarını yüklediğinden emin ol (`Reload Maven Project`), ya da Maven classpath'ini kullanarak çalıştır.

---

## 12) Sık yapılan hatalar

| Hata | Sebep | Çözüm |
|---|---|---|
| `NoClassDefFoundError` | Spring kütüphaneleri classpath'te yok | Maven bağımlılıklarını yükle, IDE'den çalıştır |
| `NoSuchBeanDefinitionException` | `getBean("...")` içindeki ad XML'deki `id` ile aynı değil | `id` ile `getBean` adını karşılaştır |
| `byName` çalışmıyor, bağımlılık `null` | Bean `id`'si setter adıyla uyuşmuyor | `setSpecification` için `id="specification"` kullan |
| `byType`'ta hata | Aynı türden birden fazla bean var | Tek bean bırak veya başka bir yöntem kullan |
| XML dosyası bulunamıyor | Dosya adı yanlış veya `resources` klasöründe değil | Dosya adını ve konumunu kontrol et |
| Setter injection'da `property` çalışmıyor | `name` değeri setter adıyla uyuşmuyor | `setXxx` için `name="xxx"` yaz |

---

## 13) Kendini test et

Cevaplarını önce kendi kafanda ver, sonra altındaki cevapla karşılaştır.

**Soru 1:** Aşağıdaki kodda hangi tür bağlılık vardır, neden?

```java
public class OrderService {
    private EmailSender sender = new EmailSender();
}
```

<details>
<summary>Cevap</summary>

**Tight coupling.** `OrderService`, bağımlılığı olan `EmailSender`'ı kendisi `new` ile yaratıyor. E-posta yerine SMS göndermek istesen `OrderService`'in kodunu değiştirmek zorunda kalırsın.
</details>

**Soru 2:** Bu koddaki sorunu nasıl çözersin?

<details>
<summary>Cevap</summary>

1. `MessageSender` adında bir interface tanımla.
2. `EmailSender` ve `SmsSender` bu interface'i uygulasın.
3. `OrderService`, `MessageSender` türünde bir bağımlılığı **constructor ile dışarıdan** alsın.
</details>

**Soru 3:** IoC'de nesneyi kim yaratır?

<details>
<summary>Cevap</summary>

**Spring container.** Sen sadece `getBean(...)` ile nesneyi istersin.
</details>

**Soru 4:** Bir `Car`, `Engine` olmadan hiç anlamlı değil. Hangi enjeksiyonu seçersin?

<details>
<summary>Cevap</summary>

**Constructor injection.** Bağımlılık zorunlu olduğu için nesne yaratılırken verilmesi gerekir.
</details>

**Soru 5:** `autowire="byName"` kullanıyorum ve sınıfımda `setEngine(...)` var. Bean `id`'si ne olmalı?

<details>
<summary>Cevap</summary>

`engine`. Spring, setter adından `set` önekini atıp ilk harfi küçülterek bean `id`'sini arar.
</details>

**Soru 6:** Container'da iki tane `Specification` bean'i var. `byType` neden sorun çıkarır?

<details>
<summary>Cevap</summary>

Spring türe göre eşleştirme yapar ve iki aday bulur. Hangisini seçeceğini bilemediği için hata verir.
</details>

---

## 14) Son özet

Bu projenin tek cümlelik ana fikri:

> **Kod kendi bağımlılığını kendi yaratmaya başladığında bağlantı sertleşir; bağımlılık dışarıdan verildiğinde ise sistem esnekleşir.**

Spring IoC de tam olarak bunu kolaylaştırır: nesneleri senin yerine yaratır, birbirine bağlar ve yönetir.

### Aklında kalması gereken 5 madde

1. 🔴 **Tight coupling:** Sınıf bağımlılığını `new` ile kendisi yaratır → değiştirmek ve test etmek zor.
2. 🟢 **Loose coupling:** Interface kullan, bağımlılığı dışarıdan ver → esnek ve test edilebilir.
3. 🌱 **Spring IoC:** Nesneleri sen değil, container yaratır ve yönetir.
4. 💉 **Constructor vs Setter:** Zorunlu bağımlılık → constructor, opsiyonel bağımlılık → setter.
5. 🤖 **Autowire:** Bağlamayı Spring'e bırak (`byName`, `byType`, `constructor`).

Başarılar! Hazırlayan: Ecem Nur ÖZEN
