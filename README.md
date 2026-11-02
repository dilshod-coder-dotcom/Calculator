# Boshqa sotuvlar daftari

Apilka va boshqa narsalar sotuvini, nasiyani va ish joylaridagi qoldiqni hisoblaydigan, internetsiz ishlaydigan telefon ilovasi. Muhāsib'dan alohida ilova.

- Sotuv: sana, ish joyi, narsa, miqdor, narx, xaridor ismi va telefoni (majburiy); ombordan yoki to'g'ridan-to'g'ri
- To'lov: naqd, karta, nasiya (qaytarish muddati, qisman to'lovlar, muddati o'tganlar)
- Ombor: har bir ish joyida kirim, qoldiq, sanash; kamomad va ortiqcha avtomatik yoziladi
- Narsalar: apilka va istalgan boshqa narsa, birligi, odatiy narxi, omborda sanaladimi
- Hisobot: davr, manba va to'lov turi bo'yicha; Excel (.xlsx) va PDF
- Zaxira nusxa (.json) va tiklash

Ma'lumotlar faqat telefonda saqlanadi, bu repository'ga hech qachon tushmaydi.

`src/index.src.html` — kutubxonalarsiz manba fayl; `index.html` — `python3 build.py` bilan yig'ilgan, hammasi ichiga joylangan versiya.

## Android ilova (APK)
`apk/` papkasida Capacitor loyihasi bor. GitHub Actions («APK (Boshqa sotuvlar)», `boshqa-sotuvlar` tarmog‘ida) imzolanmagan APK yig'adi; u GitHub'dan tashqarida saqlanadigan kalit bilan imzolanadi. APK ochiq tarqatilmaydi.
