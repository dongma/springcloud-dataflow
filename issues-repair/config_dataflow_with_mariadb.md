### 使用`MySQL`配置`Spring Cloud Dataflow`

> `Dataflow`默认情况下是使用`H2`作为数据源进行存储`dataflow`操作过程中的数据，这种带来的问题会是当系统重启后之前`register`的数据就不存在，则需使用持久化数据源。目前从官网文档获知，其支持的数据源有`MariaDB`(`MySQL`)的分支；

自定义数据源配置文档地址：`https://docs.spring.io/spring-cloud-dataflow/docs/1.7.4.RELEASE/reference/htmlsingle/#getting-started`

在项目的`/issues-repair/docker-compose.yml`经过自定义的应用启动脚本，目前启动`dataflow`服务可以通过两种方式：第一种是通过`docker-compose`进行启动，第二种是直接是用本地`jar`包进行启动。两种方式都使用过，都可以使用`MariaDB`作为持久化数据源。

1. 通过`docker-compose`启动，将`docker-compose.yml`文件放置到宿主机的某一目录，使用命令`DATAFLOW_VERSION=1.7.3.RELEASE docker-compose up -d`进行启动，目前使用`1.7.3.RELEASE`版本。可以使用`docker logs dataflow-server`查看`cloud-dataflow`服务的启动日志，是正常启动的。

   ![1568738339274](https://raw.githubusercontent.com/SamMACode/springcloud-dataflow/master/doc/images/1568738339274.png)

2. 在浏览器中输入`localhost:9393/dashboard`地址，可以看到应用时正常启动的。可以通过`docker`进入`mariadb`数据库容器中，查看数据表的创建情况。从`dataflow`创建的数据表中是存在`URI_REGISTRY`表，对于`docker-compose`服务停止时需使用`docker-compose down`进行停止：

   ```shell
   # 使用docker exec命令进入bash命令行
   > docker exec -it springcloud-dataflow_mysql_1 /bin/bash
   ```

   ![1568739084725](https://raw.githubusercontent.com/SamMACode/springcloud-dataflow/master/doc/images/1568739084725.png)

3. 第二种方式是直接通过`jar`包在本地启动应用程序，通过参数传递`cloud dataflow`的相应配置，直接通过`jar`启动暂时还需调试`MariaDB`权限验证未通过：

   ```shell
   # 1.在宿主机启动mariadb服务,同时进入docker容器内容创建`dataflow`的数据库
   docker run -p 3306:3306 -e MYSQL_ROOT_PASSWORD=rootpw --name mariadb -d mariadb:10.2
   
   # 2.通过spring-boot的命令行参数对参数进行overrite
   java -jar spring-cloud-dataflow-server-local-1.7.3.RELEASE.jar --spring.datasource.url=jdbc:mysql://localhost:3306/dataflow --spring.datasource.username=root --spring.datasource.password=rootpw --spring.datasource.driver-class-name=org.mariadb.jdbc.Driver
   ```

   ![1568740375402](https://raw.githubusercontent.com/SamMACode/springcloud-dataflow/master/doc/images/1568740375402.png)

   