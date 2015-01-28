# Známky Purkyňka
Android klient pro ISAS server Střední průmyslové školy elektrotechnické a informačních technologií Brno.

Web: http://znamky.matskiv.com/

Facebook: https://www.facebook.com/ZnamkyPurkynka

Tato verze je teď nefunkční. Doufám že nějaký stávající student se toho ujme a chybu opraví.

Postup:

1. Vytvořit Fork repositáře a stáhnout ho do PC.

2. Přidat "ActionBarSherlock-master" a "Android-ViewPagerIndicator-master" jako projekty do IDE. Oba musí mít zaškrtnuté: Properties -> Android -> Is library.

3. Přidat "Znamky" jako projekt. Dále v: Properties -> Android -> Library -> Add zvolit dva předchozí projekty a přidat jako knihovny pro tento. Plus přidat: Properties -> Android -> Java build path -> Libraries -> Add Jars -> Znamky -> libs -> jsoup-1.6.1.jar.

4. Najít a opravit chybu :). Push na github, pull request na tento repositář (samozřejmě až si budete jisti že je to opravené a funkční). Já to pak projdu a vydám nový instalační baliček.

Kontaktovat mně můžete přes skupinu na facebooku (https://www.facebook.com/ZnamkyPurkynka) nebo emailem: znamky@matskiv.com.
