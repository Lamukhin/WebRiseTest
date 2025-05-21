Запуск из командной строки в папке проекта:  
docker compose --env-file ./secret_information.env up --build -d

VM Options для запуска через IDE:  
-Ddatasource_url=localhost:5432  
-Ddatasource_database_name=webrise_test_task  
-Ddatasource_username=postgres  
-Ddatasource_password=SomePassword1997  
-Dspring.datasource.url=jdbc:postgresql://localhost:5432/webrise_test_task  
-Dspring.datasource.username=*****  
-Dspring.datasource.password=*****  


