# Start:

Приложение ExcelSeeker - парсер xls\xlsx таблиц с данными в БД и выборкой данных из БД по запросу поисковой строки с веб-сайта.
___
## БД:
Docker для БД Postgres ставится из папки ```./seeker/docker-compose.yaml```
![img.png](img.png)

Далее к БД можно подключаться из приложения.
Elasticsearch, на будущее, так же в этом файле, но не обязателен для работы.
___
## Запуск приложения:
Через главный класс ```EventsApp```, как обычное приложение Java.
___
### Front: ```http://angelilz.beget.tech/```
### Back: ```https://my.adminvps.ru/```
___
#### Swagger local: ```http://localhost:8080/seeker/swagger-ui/index.html```
#### Swagger remote: ```http://185.198.152.81:8080/seeker/swagger-ui/index.html```
_(логин-пароль от сервисов получать у разработчиков)_
___
