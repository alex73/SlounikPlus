Aleś Bułojčyk (alex73mail@gmail.com), Uladź Koščanka (koshul@gmail.com)

Collaborative software for dictionary creation SloŭnikPlus
==========================================================

The articles describes the free dictionary creation software *SloŭnikPlus* which allows compiling dictionaries of different types on the new basis. *SloŭnikPlus* is based on modern technologies and significantly improves dictionary creation process. It makes work faster, comfortable, and more effective.

**Keywords**: SloŭnikPlus, software, dictionary creation, dictionary compilation, dictionary, lexicography, lexicography software, XSD, XML, database



Most dictionaries are created using standard text processors like Microsoft Word or LibreOffice Writer. That means, that modern computers are used like typewriters with more sophisticated functions. On the other hand, it is difficult to find free and easily accessible tools for creating dictionaries. Most of the free tools, which can be found online, are too simple to compile complex dictionaries and all of them lack of teamwork functions. Some platforms, that allow making more developed dictionaries, are available only after registration and other procedures but very often they are not customizable enough.

*SloŭnikPlus* may be adapted to make dictionaries of almost any type. As far as cataloging is technically very similar to dictionary articles creation, *SloŭnikPlus* is suitable for making catalogues too.

This software was created for the Jakub Kolas Institute for Linguistics, and was used for following dictionaries creation:
- Тлумачальны слоўнік беларускай мовы (Explanatory Dictionary of the Belarusian Language)
- Словарь бѣлорусскаго нарѣчія (Dictionary of the Belarusian Dialect) by Ivan Nasovič
- Італьянска-беларускі слоўнік (Italian-Belarusian Dictionary)
- Беларуска-Кітайскі і Кітайска-Беларускі слоўнікі (Belarusian-Chinese and Chinese-Belarusian dictionaries)

*SloŭnikPlus* (https://github.com/alex73/SlounikPlus/) is based on modern technologies and significantly improves dictionary creation process. It makes work faster, comfortable, and more effective in comparison with usual dictionary creation.

Typical process of dictionary creation with the help of *SloŭnikPlus* is quite traditional: articles writers prepare articles, editors correct articles and export them for printing. But writers and editors work using special software.

A dictionary (as a list of articles) is stored in a **central server**. It requires an Internet connection if you want to work from home or other places, but it’s not an issue today. Traffic between server and workstation is very low, i.e. 3G mobile connection will be enough for work. If necessary, software can be improved for git usage to support full offline work with occasional synchronization. In this case, you may even not need a central server, you will be able to use any git repository instead.

![Central server interaction](./doc/image1.png?format=raw)
Central server interaction

![Possible Git repository interaction](./doc/image2.png?format=raw)
Possible Git repository interaction

Articles storage in a central server removes problem of **version mess**. Situation “I send the latest version, then edit a previous version, and now I don’t understand where are the right changes” is impossible.

It also makes work **manageable**. It is always clear, how many articles are ready, how many are in work, and other statistical data. You can’t forget to do something for an article: all remarks will be saved, you can add notes to a writer, propose some changes and so on. Then you will be notified about the changes in the article you work with or you are interested in.

*SloŭnikPlus* provides **data integrity**, what was impossible previously when using Microsoft Word, Microsoft Excel etc. You will be sure that nobody can change articles after its approval, nobody can change articles of other writers. You always can see a full article **history** and check who and when made all the changes. You can even have such a history after a dictionary is published: history can be exported to usual files, saved for the future, and can be reviewed locally by a standard git software.

**Structure** of an article is described using XSD (XML Schema Definition) and each article is stored as XML. XML has a quite simple structure and each user can review it using any text editor. It is good to have a defined article structure before dictionary creation starts not to update already created articles. But the admin can change a structure of an article anytime by editing XSD using usual text editor. After that, all new fields will be visible on UI for writers because *SloŭnikPlus* constructs UI automatically by defined XSD.

![Screenshot of an article editing from the Italian-Belarusian dictionary](./doc/image3.png?format=raw)
Screenshot of an article editing from the Italian-Belarusian dictionary

Sometimes, developers plan to use database tables to store a structured dictionary article in a relative database. At first glance, using a relative database is a good idea (even if we forget that XSD can describe a structure much better than a database structure) ‒ articles can be found by the SQL SELECT command. But real users don’t know SQL commands, and nobody will use SQL for search articles. Instead, they will use some UI. And in case you need to change an article structure it will not be so simple ‒ you need to ask a developer to change a database structure, then probably rewrite software for a new database structure and a new UI. It requires a lot of time, work and money. In addition, access to such a database will be much slower since software will need to call a database server many times to request one article. You will also need to support some specific database software, use some software to backup/restore a database to see the dictionary. If you need an access to the articles structure in future (e.g., to make a new search system), it will take much work to restore a database backup and call database requests for a 15-years old database. Compare this process with much more simple XML files reading.

*SloŭnikPlus* allows to have a strict articles **validation**. First of all, the structure will be the same for all the articles in the dictionary, i.e. it’s impossible to have old articles with some old structure and newer articles with a new structure. Validation can check an article structure, special marks, characters, spelling (optionally using LanguageTool), fields content and some more complex things, like cross-reference links between articles, or correspondence between several fields within an article. In addition to a simple cross-reference check, in the course of work with an article, a writer will always see which other articles refer to this article. Validation can be improved during dictionary writing, and executed for all existing articles by one click. There is no need to change software for new validation rules ‒ rules can be changed by means of javascript file editing.

**Flow and statuses**. Since dictionary writing is not a simple process, and includes writers, editors, consultants and other staff, a team normally has some workflow for articles in a dictionary. For example, the simplest workflow can be just a rule: a writer should send an article to an editor for approvement. It looks simple, but it never happens so simple: editor will want to return some article for rework, or send it to somebody for special review, etc. *SloŭnikPlus* supports such a workflow for a dictionary. It supports *any* workflow. You can define your own workflow for your team, and each article will go through a defined workflow ‒ you will be sure that *all* articles are **reviewed** and **fixed**. An admin can setup a workflow by XML file editing ‒ you don’t need a developer for that work. And a workflow can be changed anytime after a dictionary is started.

![An example of a workflow](./doc/image4.png?format=raw)
An example of a workflow

A flow is applied to each article. That means (opposite to the current dictionary editing process), writers do not need to wait editors to fully review a part of a dictionary. Every article goes through a workflow immediately, i.e. state of a dictionary is always actual. The situation “I have almost done but I will send the results in the next week” is impossible.

Articles should be **exported** to be printed on paper. Each article in a dictionary should have a predefined structure declared by XSD (i.e., tree-like structure). Paper dictionaries usually look like text where each article is a separate paragraph or several paragraphs with italic, bold styles and other special marks. Additionally, to define an article structure, you need to define javascript to export an article structure into a html-like text (it allows italic, bold, special marks and almost all other html features). This script can be changed anytime, and full dictionary can be exported using new look just by one click. The original XML of dictionary articles can be used in a web-based version of a dictionary for to be able to search by some specific fields. For example, if you have a field for a part of speech, you can allow a user to choose part of speech for search in your dictionary web UI.

*SloŭnikPlus* is a **free software** (licensed under GPLv3). That means you have all benefits of a free software: you don’t need to create an application from scratch, you can improve the software without rewriting from scratch, you can use improvements from other teams. The architecture is pluggable and you can create some specific UI for your specific dictionary fields. Both the client and the server parts were written using Java ‒ you can use it under Windows, Linux or Mac.

## Entities
For start dictionary creation, you need to decide some basic things: language of dictionary keywords, article structure (dictionary can have more than one acticle types), processing flow and user's roles.

## Deployment
*SloŭnikPlus* client is a simple Java Swing application, that require Java 8. You can use https://github.com/alex73/MiniWebStart or other tools for have client computers up to date, or just install client jars on user's computers.

Server part requires to have Tomcat 9 or other java web server.

## Customization
All things for customize *SloŭnikPlus* are stored in the config/ directory.

- db.properties - declares database connection. PostgreSQL is usually used. You need to run commands from DB_STRUCTURE.sql for initialize database structure.
- config.xml - declares roles, users, flow, article types and permissions.
- <article_type>.xsd - each article type should have own structure declaration. Name of root element should be the same like article type name.
- <article_type>.js - script for prepare article output and validate article data.
- <article_type>-summary.js - script for prepare summary and check errors, based on all articles.

## Plugins
If you are creating some complex dictionary, you can create some custom controls, use some external sources, add some custom menus and other plugins. All this things can be compiled with *SloŭnikPlus* client side and server side code and can be used with main application. See MANIFEST.MF for declare plugin initialization classes.
