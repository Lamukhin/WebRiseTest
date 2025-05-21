Когда я приступил к выполнению, то взял за основу миграции FlyWay и JOOQ.  
При каждом запуске сверяется версия миграции и после этого происходит сверка сгенерированных классв.
Когда я стал упаковывать приложение в Docker, то выяснилось, что в изолированной среде без
доступа к существующей базе это не сделать. Файлы для Docker я оставил, чтобы было понятно, что
я имею представление о том, как с этим работать, но в данном случае они не исползьуются. 
Предлагаю просто запустить через IDEA, указав параметры запуска вручную. Так же я использовал у себя 
jdk 21, это надо исправить в gradle.build, если у вас отличная версия.
VM Options для запуска через IDE:  
-Ddatasource_url=localhost:5432  
-Ddatasource_database_name=webrise_test_task  
-Ddatasource_username=postgres  
-Ddatasource_password=SomePassword1997  
-Dspring.datasource.url=jdbc:postgresql://localhost:5432/webrise_test_task  
-Dspring.datasource.username=*****  
-Dspring.datasource.password=*****  


