## Aston Intensive HW 2. Servlet JDBC CRUD API

### Описание проекта

Проект представляет REST API на основе сервлетов и JDBC.
Предоставляет как стандартный набор CRUD-операций, 
так и расширенные возможности 
с организацией связи ManyToMany
на основе двух таблиц: Projects и Users

- используются не PathVariable, составляющие часть пути к запросу, а передаваемые параметры (query params)


- По эндпойнту /api/v1/users происходит создание, удаление, редактирование двнных пользователя
(используется строковой параметр 'id'), а также запрос информации о пользователе (по 'id')


- По эндпойнту /api/v1/projects происходит создание, удаление, редактирование двнных проекта
  (так же используется строковой параметр 'id'), а также запрос информации о проекте (по 'id' проекта)

### Организована связь Many To Many

- Каждый пользователь может принимать участие в многих проектах; 
- К каждому проекту может присоединиться множество пользователей.

Для организации взаимосвязи добавлена вспомогательная таблица project_users,
содержащая user_id и project_id (составной Primary key на основе этих двух столбцов)

- По эндпойнту /api/v1/users/all получаем список всех пользователей, В полученном списке внутри каждого объекта User 
содержится коллекция DTO проектов, в которых пользователь
принимает участие, а также, каждый DTO проекта с коллекцию Id пользователей-участников проекта.

### Организована связь One To Many

- По эндпойнту /api/v1/projects/user по передаваемому в адресной строке параметру 'id' 
получаем коллекцию проектов, в которых принимает участие пользователь;


- По эндпойнту /api/v1/projects/admin получаем коллекцию проектов, в которых пользователь является администратором (создателем проекта),
так же, на основании переданного 'id'

### Добавление / удаленик пользователя в / из проекта

- По эндпойнту /api/v1/projects/users методами POST и DELETE добавление / удаление пользователя:
в адресной строке передаются два параметра - userid и projectid, которые заносятся во вспомогательную таблицу project_users

### Дополнительно 

Попытался сделать асинхронные запросы к базе данных через созданный пул потоков;
Из репозитория возвращается объект CompletableFuture.

### Но

Но, уже добравшись до слоя сервлетов, пришло осознание сделанной ошибки:
из CompletableFuture я получал результат в слое контроллера, а , начав создавать слой сервлетов,
увидел, что стоило брать создание потоков от сессии в сервлете....

### Ещё одно НО

Пришлось прибегнуть к синхронноым запросом в БД в двух ситуациях:

- /api/v1/projects/user - при запросе пользователя с его проектами
- /api/v1/projects/admin - при запросе администратора ведомых им проектов

Тут не не хватило времени допилить асинхронный функционал до требуемой работоспособности,
по этому в целях экономии времени написал логику работы в однопоточном режиме.

### P.S.

- Тесты сервисного слоя;
- Задокументировал сколько успел
- на сколько успел "причесал" код
