# Tight vs Loose Coupling ve Spring IoC Ornekleri

Bu proje, **bagimlilik yonetimi** konusunu adim adim anlatan bir ogrenme notu gibi tasarlanmistir.

Amac su sorulara net cevap vermektir:

- Neden **tight coupling** problem yaratir?
- **Loose coupling** nasil kurulur?
- Spring IoC container nesneleri nasil yonetir?
- **Constructor injection**, **setter injection** ve **autowire** ne zaman kullanilir?

## Ogrenme sirasi

Projeyi en iyi su sirayla okursun:
1. `com.tight.coupling`
2. `com.loose.coupling`
3. `car.example.bean`
4. `car.example.constructor.injection`
5. `car.example.setter.injection`
6. `car.example.autowire.name`
7. `car.example.autowire.type`
8. `car.example.autowire.constructor`

## 1) Tight Coupling

### Kod ne yapiyor?
`com.tight.coupling.UserManager`, dogrudan `UserDatabase` olusturuyor:

```java
private UserDatabase userDatabase = new UserDatabase();
```

Bu yaklasimda `UserManager`, sadece tek bir veri kaynagiyla calisir.

### Sorun ne?

- Degistirmesi zordur.
- Test etmek zordur.
- Farkli kaynaklar eklemek icin sinifi degistirmek gerekir.

Kisaca: **sınıf, kendi bagimlisini kendi yaratıyorsa tight coupling olur.**

## 2) Loose Coupling

`com.loose.coupling` paketinde bagimlilik bir arayuz ile soyutlanir:

- `UserDataProvider` = sozlesme
- `UserDatabaseProvider` = veritabani implementasyonu
- `WebServiceDataProvider` = web servisi implementasyonu
- `UserManager` = somut sinifa degil arayuze baglidir

### Neden daha iyi?

`UserManager`, hangi kaynaktan veri geldigini bilmek zorunda degildir.

Bu sayede:

- yeni kaynak eklemek kolaylasir
- test etmek kolaylasir
- kod daha esnek olur

### Buradaki temel fikir

```java
public UserManager(UserDataProvider userDataProvider)
```

Bu satir, **dependency injection** mantigini gosterir: bagimlilik disaridan verilir.

## 3) Spring IoC ile bean yonetimi

`car.example.bean` paketi, Spring container'in nesneleri nasil yonettigini gosterir.

### Dosyalar

- `MyBean` = basit bean
- `App` = Spring context'ten bean alma ornegi
- `applicationBeanContext.xml` = bean tanimi

### Ogrenilen kavram

Spring, nesneleri senin yerinize yaratir ve yonetir.

Yani:

```java
ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationBeanContext.xml");
MyBean myBean = applicationContext.getBean("myBean", MyBean.class);
```

Burada nesne, `new` ile degil container ile gelir.

## 4) Constructor Injection

`car.example.constructor.injection` paketi, bagimliligin constructor ile verildigini gosterir.

### Mantik

- `Car`, `Specification` olmadan eksiktir.
- Bu nedenle bagimlilik constructor uzerinden zorunlu verilir.

### XML tarafi

`applicationConstructionInjection.xml` icinde:

```xml
<bean id="myCar" class="car.example.constructor.injection.Car">
    <constructor-arg ref="carSpecification"/>
</bean>
```

### Ne zaman tercih edilir?

- Bagimlilik zorunluysa
- Nesne, eksik durumda anlamli degilse

## 5) Setter Injection

`car.example.setter.injection` paketi, bagimliligin setter ile verildigini gosterir.

### Mantik

`Car` once olusur, sonra `setSpecification(...)` ile tamamlanir.

```java
public void setSpecification(Specification specification) {
    this.specification = specification;
}
```

### Ne zaman tercih edilir?

- Bagimlilik opsiyonelse
- Nesne sonradan degisebilir durumda ise

## 6) Autowire

Autowire, Spring'in beanleri elle baglamadan otomatik eslestirmesidir.

Bu projede 3 farkli yol var:

### a) byName

`autowireByName.xml`

Spring, setter adi ile bean id'sini eslestirir.

Ornek:

- setter: `setSpecification(...)`
- bean id: `specification`

### b) byType

`autowireByType.xml`

Spring, tip uzerinden eslestirme yapar.

### c) constructor

`autowireByConstructor.xml`

Spring, constructor parametre tipine gore bagimliligi bulur.

## Klasor ozeti

| Paket / dosya | Amac |
|---|---|
| `com.tight.coupling` | Dogrudan bagimli tasarim |
| `com.loose.coupling` | Interface ile gevsek bagimlilik |
| `car.example.bean` | Spring bean ve XML config |
| `car.example.constructor.injection` | Constructor injection |
| `car.example.setter.injection` | Setter injection |
| `car.example.autowire.*` | Autowire turleri |

## XML dosyalari

| Dosya | Aciklama |
|---|---|
| `applicationBeanContext.xml` | Basit bean tanimi |
| `applicationConstructionInjection.xml` | Constructor injection |
| `applicationSetterInjection.xml` | Setter injection |
| `applicationIoCLooseCouplingExample.xml` | Loose coupling + Spring IoC |
| `autowireByName.xml` | Autowire by name |
| `autowireByType.xml` | Autowire by type |
| `autowireByConstructor.xml` | Autowire by constructor |

## Calistirma notu

Proje Maven ile yazildi ve Java 21 hedefliyor.

Spring ornekleri calistirirken IDE uzerinden veya Maven classpath'i ile calistirman gerekir; yoksa `NoClassDefFoundError` gorebilirsin.

## Ogrenme ozeti

Bu proje su ana fikri ogretir:

**Kod kendi bagimliligini kendi yaratmaya basladiginda baglanti sertlesir; bagimlilik disaridan verildiginde ise sistem esneklesir.**

Spring IoC de tam olarak bunu kolaylastirir.
