[TOC]
## SpringCloud-Dataflow技术调研
### 一、SpringCloud-Dataflow简要介绍
Spring Cloud Dataflow 是基于原生云对 Spring XD 的重新设计，该项目目标是简化大数据应用的开发。 Cloud- Dataflow 简化了专注于数据流处理的应用程序的开发和部署，它的体系结构包含的主要概念有：微服务应用程序、Data Flow Server 和运行时环境，Cloud Dataflow 为基于微服务的分布式流处理和批处理数据通道提供了一系列模型和最佳实践。
1. Cloud Dataflow 是一个用于开发和执行大范围数据处理其模式包括ETL，批量运算和持续运算的统一编程模型和托管服务。
2. 对于在现代运行环境中可组合的微服务程序来说，Spring Cloud Dataflow是一个原生云可编配的服务。使用Spring Cloud Dataflow，开发者可以为像数据抽取，实时分析，和数据导入/导出这种常见用例创建和编配数据通道 （data pipelines）。
3. Spring XD 的流处理和批处理模块的重构分别是基于 Spring Cloud Stream 和 Spring Cloud Task/Batch 的微服务程序。这些程序现在都是自动部署单元而且他们原生的支持像 Cloud Foundry、Apache YARN、Apache Mesos和Kubernetes 等现代运行环境。
4. Spring Cloud Dataflow 为基于微服务的分布式流处理和批处理数据通道提供了一系列模型和最佳实践。

### 二、Cloud-Dataflow环境搭建
#### 1. 下载docker-compose.yml文件
```shell
wget https://raw.githubusercontent.com/spring-cloud/spring-cloud-dataflow/v1.7.4.RELEASE/spring-cloud-dataflow-server-local/docker-compose.yml
```
可以自定义docker-compose.yml配置文件内容：
1. 使用RabbitMQ替换Apache Kafka作为消息中间件；
2. 使用MySQL代替DataFlow默认的H2 database；
3. 要使用redis作为后端启用实时数据流分析，则需要在docker-compose中添加redis的配置。

#### 2. 启动docker-compose
在Linux系统中启动docker-compose服务：
```shell
bash> DATAFLOW_VERSION=1.7.4.RELEASE docker-compose up -d
```
在windows环境中启动docker-compose服务：
```shell
C:>  set DATAFLOW_VERSION=1.7.4.RELEASE
C:>  docker-compose up -d
```
注意：有时由于docker的版本比较低，启动docker-compose是会提示`version:'3'`不支持，可以选择升级docker的版本 或者将 docker-compose.yml文件的首行的版本号调整为`version:'2'`

#### 3. Cloud Dataflow Dashboard后台UI
docker-compose服务启动之后，可以通过访问`http://127.0.0.1:9393/dashboard`进行Dataflow管理界面：

![1550163620653](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550163620653.png)

#### 4. 使用Maven在本地安装Cloud Dataflow环境
```shell
# 从maven镜像中心获取spring-cloud-dataflow-server-local-1.7.4.RELEASE.jar
bash> wget https://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-server-local/1.7.4.RELEASE/spring-cloud-dataflow-server-local-1.7.4.RELEASE.jar
# 从镜像中心获取spring-cloud-dataflow-shell-1.7.4.RELEASE.jar
bash> wget https://repo.spring.io/release/org/springframework/cloud/spring-cloud-dataflow-shell/1.7.4.RELEASE/spring-cloud-dataflow-shell-1.7.4.RELEASE.jar
```
在本地运行`spring-cloud-dataflow-server-local-1.7.4.RELEASE.jar`，启动后访问`http://127.0.0.1:9393/dashboard`也可以进入到管理界面。在使用`jar`包进行启动时需要在本地环境需要启动`MySQL`、`Kafka`、`Redis`等服务。

```shell
bash> java -jar spring-cloud-dataflow-server-local-1.7.4.RELEASE.jar
```
![1550165201543](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550165201543.png)

虽然`Cloud Dataflow`提供了管理界面用于操作数据流，管理`task`任务，但有时并不是很高效。通过`cloud-dataflow-shell`可以完成图形化界面相同的工作，如`register application`，`deploy stream`，`deploy task`等等。可以在本地启动`spring-cloud-dataflow-shell-1.7.4.RELEASE.jar`完成与管理界面相同的工作。
```shell
bash> java -jar spring-cloud-dataflow-shell-1.7.4.RELEASE.jar
```
如果`cloud-dataflow-server`和`cloud-dataflow-shell`没有在同一个`host`下，可以将`shell`指向数据服务器的`url`地址：
```shell
bash> dataflow config server `http://192.168.100.86`
bash> Successfully targeted `http://192.168.100.86`
dataflow:> 
```
![1550166285559](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550166285559.png)

默认情况下应用不会自动注册，如果想要注册所有和kafka集成的开箱即用的流应用，可以使用如下命令注册应用：
```shell
dataflow:> app import --uri `http://bit.ly/Bacon-RELEASE-stream-applications-kafka-10-maven`
```

#### 5. Cloud Dataflow Shell 常用的命令
* __app list：__ 查看当前`Dataflow`注册的所有应用信息：

![1550168910934](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550168910934.png)

* __app info：__ 查看`time`应用的基本信息（`app info source:time`）：

![1550169071672](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550169071672.png)

* __app register: __ 向`Dataflow`中注册应用信息，`Dataflow` 注册应用主要有两种方式：通过`jar`的路径注册、通过`maven`仓库地址注册应用信息：
```shell
# 通过指定app.jar的路径进行注册，在windows系统上不易使用
dataflow:>app register --type source --name my-app --uri file://root/apps/my-app.jar
```
```shell
# 从maven镜像中心注册pose-estimation-processor-rabbit应用
#（若为本地应用 可以通过maven install 安装在本地仓库，之后再向dataflow中进行注册）
dataflow:>app register --type processor --name pose-estimation --uri maven://org.springframework.cloud.stream.app:pose-estimation-processor-rabbit:2.0.2.BUILD-SNAPSHOT
```

![1550170091573](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550170091573.png)

* __app unregister: __ 从`Cloud Dataflow`中移除已经注册的应用app：
```bash
dataflow:> app unregister --type sink --name gemfire
dataflow:> Successfully unregistered application 'gemfire' with type 'sink'
```
* __help:__ 查看`Cloud Dataflow`中所有指令的用法：
```bash
dataflow:> help
```

![1550372171722](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550372171722.png)

### 三、Cloud-Dataflow 流式处理

__SpringCloud Stream介绍：__ `Spring Cloud Stream`是创建消息驱动微服务应用的框架，`Spring Cloud Stream`基于`Spring Boot`建立独立的生产级`Spring`应用程序，并使用`Spring Integration`提供与消息代理的连接，主要用于分布式系统中微服务组件之间通信。`Spring Cloud Stream `目前提供了与`RabbitMQ`、`Apache Kafka`消息中间件的整合。

__Spring Cloud Stream应用模型：__ `Spring Cloud Stream`由一个中立的中间件内核组成。`Spring Cloud Stream`会注入输入和输出的`channels`，应用程序通过这些`channels`与外界通信，而`channels`则是通过一个明确的中间件`Binder`与外部`Brokers`连接。

![1550375366340](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550375366340.png)

#### 1. Spring Cloud Stream使用
根据`Spring Cloud Stream App Starters`文档内容，下载`log-sink-kafka-10-1.3.0.RELEASE.jar`，`time-source-kafka-10-1.3.0.RELEASE.jar`(在运行环境需要启动Kafka)；
```bash
# 启动time-source-kafka，在启动参数中指定消息输出消息的队列
bash> java -jar time-source-kafka-10-1.3.0.RELEASE.jar --spring.cloud.stream.bindings.output.destination=ticktock
# 启动log-sink-kafka，在启动参数中指定接收消息的队列
bash> java -jar log-sink-kafka-10-1.3.0.RELEASE.jar --spring.cloud.stream.bindings.input.destination=ticktock --server.port=8081
```
启动应用后，可以观察到`log-sink-kafka`在控制台上一直在打印当前系统时间（消费Kafka消息队列中消息）：

![1550374961839](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550374961839.png)

#### 2. Spring Cloud Stream App 类型
1. __source__:  数据源主要用于向消息中间件中发送消息，需要指定消息的输出地址（指定 `output.destination`）；
2. __processor__:  从消息中间件中消费消息，之后对消息进行中间处理，之后将消息发送到指定的输出队列（同时指定 `input.destination`和`output.destination`）；
3. __sink__:  从消息中间件中指定消息队列中消费消息（指定`input.destination`）； 

#### 3. Cloud Dataflow 自定义Stream流处理
在`Cloud Dataflow`中可以对`Spring Cloud Stream App`进行自定义流式处理。使用`Cloud Dataflow`示例：对于`source.http`应用接收`8787`端口的请求，对数据进行简单处理后，将其发送到kafka消息队列，`sink.jdbc`从kafka中消费消息，并且将数据存放到`Mariadb`数据库中；
1. 启动kafKa、zookeeper以及mariadb
```bash
# 启动spring-cloud-dataflow-server-local-1.7.4.RELEASE.jar 
bash> java -jar spring-cloud-dataflow-server-local-1.7.4.RELEASE.jar
# 在Docker中启动Mariadb数据库
bash> docker run -p 3307:3306 --name mariadb -e MYSQL_ROOT_PASSWORD=root -d mariadb:10.2
# 启动spring-cloud-dataflow-shell工具
bash> java -jar spring-cloud-dataflow-shell-1.7.4.RELEASE.jar
# 启动本地zookeeper以及apache Kafka
C:> zkServer
D:> .\bin\windows\kafka-server-start.bat .\config\server.properties
```
2. 连接到mariadb创建`test`数据库以及`names`数据表
```sql
# 通过docker命令连接maridb数据库
bash> docker exec -it [maridb-container-id] bash
bash> mysql -uroot -proot
# 创建数据库test并创建数据表names
create database test;
use test;
create table names ( name varchar(255) );
```
3. 向`Cloud Dataflow`中注册`Spring Cloud Stream`应用（`source.http`、`sink.jdbc`），使用`Cloud Dataflow`的`DSL`实现数据流定义：
```bash
# 向cloud-dataflow中注册source.http
dataflow> app register --type source --name http --uri maven://org.springframework.cloud.stream.app:http-source-kafka-10:1.3.1.RELEASE
# 向cloud-dataflow中注册sink.jdbc
dataflow> app register --type sink --name jdbc --uri sink.jdbc=maven://org.springframework.cloud.stream.app:jdbc-sink-kafka-10:1.3.1.RELEASE
# 通过Cloud-Dataflow DSL发布数据流，请求`http 8787`端口并验证请求结果
stream create --name mysqlstream --definition "http --server.port=8787 | jdbc --tableName=names --columns=name --spring.datasource.driver-class-name=org.mariadb.jdbc.Driver --spring.datasource.url='jdbc:mysql://localhost:3307/test' --spring.datasource.username=root --spring.datasource.password=root" --deploy
# 通过stream list查看mysqlstream发布状态，如果发布成功会在status显示Successful deployed
dataflow> stream list
# 在dataflow中向http应用发送请求数据，如果请求发送成功会显示[202 accepted];同时在Maridb进行验证是否有记录插入数据表；
dataflow> http post --contentType 'application/json' --target `http://localhost:8787` --data "{\"name\": \"Dataflow\"}"
```
![1550384142958](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550384142958.png)

#### 4.Spring Cloud Stream App实现
1. __Source App：__ 下面可以看到一个简单的Source应用程序，在`@InputChannelAdapter`注释的帮助下，使得程序可以将产生的消息发送到`spring.cloud.stream.bindings.output.destination`配置的输出`channel`中，`@EnableBinding`注解触发Spring Cloud Stream配置；
```java
@SpringBootApplication
@EnableBinding(Source.class)
public class SourceApp {
    private Logger LOGGER = LoggerFactory.getLogger(SourceApp.class);

    public static void main(String[] args) {
        SpringApplication.run(SourceApp.class, args);
    }

    @InboundChannelAdapter(value = Source.OUTPUT)
    public String source() {
        String date = new Date().toString();
        LOGGER.info("Source app input date: [{}]", date);
        return date;
    }
}
```
2. __Processor App:__ processor类型的应用主要用于从`channel1`中消费消息，同时将产生的消息发送到`channel2`中：
```java
@SpringBootApplication
@EnableBinding(Processor.class)
public class ProcessorApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(ProcessorApp.class);

   public static void main(String[] args) {
      SpringApplication.run(ProcessorApp.class, args);
   }

	@ServiceActivator(inputChannel = Processor.INPUT, outputChannel = Processor.OUTPUT)
    public String transform(String payload) {
        LOGGER.info("Processor received message: [{}]", payload);
	    return payload + " processed";
    }
}
```
3. __Sink App:__ Sink类型的应用主要借助于`@StreamListener`注解从`input-channel`中消费消息：
```java
@SpringBootApplication
@EnableBinding(Sink.class)
public class SinkApp {
    private static final Logger LOGGER = LoggerFactory.getLogger(SinkApp.class);

	public static void main(String[] args) {
		SpringApplication.run(SinkApp.class, args);
	}

    @StreamListener(Sink.INPUT)
    public void sinkLog(String message) {
        LOGGER.info("sink-app receive message: [{}]", message);
    }
}
```
#### 4. Cloud-Dataflow Stream DSL
`Stream DSL`使用基于`unix`的`Pipeline`（管道符）语法定义流，该语法使用“管道”来连接多个命令`ls -l | grep key | less` 在`Unix`中的`less`获取`ls -l`进程的输出并将其结果传递给`grep key`指定作为输入，`grep`的输出又被发送到`less`指令作为输入。 每个`| symbol`将左侧命令的标准输出连接到右侧命令的标准输入，将数据从左到右流过管道。

在`Spring Cloud data-flow`中，注册的`Stream App`作用就如Unix命令一样，每一个管道符通过消息中间件（`RabbitMQ`、`Kafka`）连接产生消息和消费消息的应用程序；

__创建Stream的语法：__
```bash
# 在--definition:定义流处理 --name:定义数据流的名称
dataflow:>stream create --definition "http --port=8090 | log" --name myhttpstream
```
#### 5.Cloud Dataflow 中查看应用运行日志
在部署应用以及应用运行过程中难免会出现异常，这时需要查看应用的运行日志，在`Cloud Dataflow`中可以在 管理页面中 `'Runtime-> application'`找到应用的运行日志：

![1550395192968](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550395192968.png)

#### 6.Analystic 对数据流进行实时分析
在`Cloud Dataflow`可以对当前`Stream`流中的数据进行实时分析，在 使用`Cloud Dataflow`进行数据分析功能时需要启动`Redis`服务。目前支持3种分析的维度：
1. `Counter`: 统计应用从消息队列中接收到消息的数量，其会将统计结果存放在`Redis`缓存中；
2. `Field Value Counter`: 统计从消息队列中某个字段的所有不重复的字段值；
3. `Aggregate Counter`: 统计接收到的消息的总数量，不过会分别以分钟、小时、天、月为单位存储结果；

```bash
# docker启动redis服务
bash> docker run -p 6379:6379 --name redis-dataflow -d redis:latest
# 在cloud-dataflow中注册field-value-counter应用
dataflow> app register --type sink --name field-value-counter --uri maven://org.springframework.cloud.stream.app:field-value-counter-sink-kafka-10:1.3.1.RELEASE
# 创建进行实时数据分析的Stream流
stream create http_analystic --definition ":mysqlstream.http > field-value-counter --fieldName=name --name=httpfield" --deploy
```
创建`http_analystic`数据流之后可以，通过`dataflow-shell`向`mysqlstream`中发送请求：
```bash
# 向mysqlstream中发送请求数据
dataflow> http post --contentType 'application/json' --target http://localhost:8787 --data "{\"name\": \"Dataflow\"}"
# 查看当前field-value-counter分析列表
dataflow> field-value-counter list
```
![1550411862573](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550411862573.png)

在`data flow`的管理界面查看分析结果，`Analystic | Dashboard`，在`Metric Type`选项选择`Field Value Counters` ，`Counter Name`选项选择`httpfield`，`Visualization`选项选择`Pie Chart`，最后结果如下图所示：在饼状图中展示了发送给`http`应用程序`name字段`所有不重复的字段值；

![1550412557393](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550412557393.png)


### 四、Cloud-Dataflow 任务处理
__SpringCloud Task介绍：__Spring Cloud Task 主要解决微服务中周期短的的任务以及任务调度的工作，比如“每间隔一段时间对任务进行重新执行、或者在每天的特定时刻执行任务等等”。一个任务通常作为后台进程来执行，在`Spring Cloud Task`中常常使用`@EnableTask`注解启用其功能，用户常常在主进程中发起一个任务，当任务结束时该进程也终止。一个任务通常需要记录其开始时间`startTime`、结束时间`endTime`以及任务最终执行状态`exit code`。`Cloud Dataflow`中`Task`的实现主要是基于`Spring Cloud Task`项目的。
#### 1.  Task任务的生命周期
创建一个`Spring Cloud Task`程序（依赖于spring-cloud-starter-task）
```java
@EnableTask
@SpringBootApplication
public class MyTask {
    public static void main(String[] args) {
		SpringApplication.run(MyTask.class, args);
	}
}
```
向`Cloud Dataflow`中注册一个`task`应用：
```bash
dataflow> app register --type task --name timestamp --uri maven://org.springframework.cloud.task.app:timestamp-task:1.3.0.RELEASE
```
创建一个`Task`任务，指定task任务的`--name`属性
```bash
dataflow> task create timestamp-task --definition "timestamp --format=\"yyyy\""
Created new task 'timestamp-task'
# 使用task list查看创建的任务
dataflow> task list
```
![1550417556259](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550417556259.png)

发起一个任务：一个任务可以通过`Restful Api`或者`Cloud Dataflow shell`命令行进行触发，在命令行中输入`task launch [task-name]`可以触发这个任务：
```bash
dataflow:> task launch timestamp-task
Launched task 'timestamp-task'
# 也可以在触发task任务时指定任务参数[--arguments]
dataflow:> task launch timestamp-task --arguments "--server.port=8080 --custom=value"
# 其他task触发的任务本身属性可以通过`--properties`配置进行设置
dataflow:> task timestamp-task --properties "deployer.timestamp.custom1=value1,app.timestamp.custom2=value2"
```

查看任务的执行情况：一旦一个任务执行了，任务执行的状态会放在关系型数据库中。其中包含的字段名称包括：`TaskName`，`Start Time`，`End Time`, `Exit Code`，`Exit Message`，`Last Updated Time`，`parameters`等多个字段；
```bash
# 查看所有task任务执行状态
dataflow:> task execution list
# 查看特定task任务更详细信息内容
dataflow:> task execution status --id [taskId] 
# 销毁特定的task任务
dataflow:> task destory timestamp-task
# 验证特定的task任务(有时候一个task任务中可能包含多个应用，可以对app register时的注册地址是否有效进行验证)
dataflow:> task validate timestamp-task
```
![1550465938789](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550465938789.png)

#### 2. Composed Task 任务管理
`Spring Cloud Task`数据流允许用户创建一个有向图，图中的每个节点都是一个任务应用程序。这是通过对组合任务使用`DSL`来完成的。可以通过`Restful API`、`Spring Cloud Dataflow Shell`或`Spring Cloud Dataflow UI`创建复合任务。

1. 向`Cloud-Dataflow`中注册`composed-task-runner`应用
```bash
dataflow:> app register --name composed-task-runner --type task --uri maven://org.springframework.cloud.task.app:composedtaskrunner-task:1.1.0.RELEASE
```
2. 注册多个`task`应用，创建一个`composed-task`任务并执行
```bash
# 向cloud-dataflow中注册spark-yarn task应用程序
dataflow:> app register --name spark-yarn --type task --uri maven://org.springframework.cloud.task.app:spark-yarn-task:1.3.0.RELEASE
# 向cloud-dataflow中注册timestamp task应用程序
dataflow:> app register --name timestamp --type task --uri maven://org.springframework.cloud.task.app:timestamp-task:1.3.0.RELEASE 
# 创建一个composed task
dataflow:> task create composed-task --definition "spark-yarn && timestamp" 
# 启动创建的composed-task任务
dataflow:> task launch composed-task
# 使用task list命令查看当前注册的所有应用
dataflow:> task list
# 使用task execution list可以查看composed-task任务的执行状态
dataflow:> task execution list
```

![1550508211355](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550508211355.png)

在`launch composed task`任务时，可以通过`app.<composed task definition name>.<child task app name>.<property>`的方式设置子任务的属性。
```bash
# 在执行task任务时设置timestamp.format属性的值
dataflow:> task launch composed-task --properties "app.composed-task.timestamp.format=YYYY"
# 同样也可以在task任务执行时设置deployer属性
dataflow:> task launch composed-task --properties "deployer.composed-task.spark-yarn.memory=2048m,app.composed-task.mytimestamp.timestamp.format=HH:mm:ss"
```
销毁一个`composed-task`以及停止一个`composed-task`任务，对于停止一个已经启动的`composed-task`任务，可以在`Dataflow-UI`界面上选择`Jobs | Stop the Job`（针对要进行停止的`task`任务）
```bash
# 销毁之前已经创建的composed-task任务
dataflow:> task destroy composed-task
# 在销毁composed-task任务之后，执行task list就看不到已经注销的应用
dataflow:> task list
```
![1550508986307](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550508986307.png)

#### 3. Composed task的DSL
1. 当`task1`执行成功后再执行`task2`，有依赖条件的`composed task`，通常使用`&&`操作符表示这种关系。
```bash
# 只有task1成功执行完成才会执行task2
dataflow:> task create composed-task --definiton "task1 && task2"
```

![1550511124565](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550511124565.png)

2. 当`task1`任务执行状态`Failed`时执行`bar`应用，执行状态为`Completed`则执行`baz`应用，当`task1`任务执行状态为其它时则该`composed task`任务执行结束；

```bash
dataflow:> task create composed-task --definition "task1 'Failed' -> bar 'Completed' -> baz"
# 可以使通配符'*'表示其它所有情况
dataflow:> task create composed-task --definition "task1 'Failed' -> bar '*' -> baz"
```
![1550511176552](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550511176552.png)

3. 并行执行`task`任务，使用`< || >`表示

```bash
# foo、bar、baz 3个task任务可以并行执行
dataflow:> task create parallel-task --definition "<foo || bar || baz>" 
```
![1550511230394](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550511230394.png)
4. `foo`、`bar`、`baz`应用可以并行执行，等全部执行完成后，可以并行执行`qux`、`quux`任务
```bash
dataflow:> task create my-split-task --definition "<foo || bar || baz> && <qux || quux>"'
```

![1550511479131](C:\Users\ChinaDaas\AppData\Roaming\Typora\typora-user-images\1550511479131.png) 

### 五、Cloud Dataflow调研结论
`Cloud Dataflow`在基于`Spring Cloud Stream`之上增强了对于`Stream`的数据流管理，`Cloud Dataflow`提供的`DSL`可以自定义数据流，同时其提供的实时数据分析功能也可以对数据流内容进行实时监控，其自身的`DSL`对于数据流处理提供了很大的灵活性。

对于`Task`任务的管理，`Cloud Dataflow`提供了后台管理UI可以很方便管理应用，同时其自身的`DSL`可以轻松处理多个任务之间的依赖关系，对于任务的管理提供了很大的灵活性，像一些离线的应用都可以放到`Cloud Dataflow`中对其进行统一管理。

### 六、Cloud Dataflow相关参考内容
* [Spring Cloud Data Flow Reference Guide](https://docs.spring.io/spring-cloud-dataflow/docs/1.7.4.RELEASE/reference/htmlsingle/#getting-started)
* [Spring Cloud Stream Reference Guide](https://docs.spring.io/spring-cloud-stream/docs/Chelsea.SR2/reference/htmlsingle/index.html)
* [Spring Cloud Task Reference Guide](https://docs.spring.io/spring-cloud-task/docs/2.0.0.RELEASE/reference/htmlsingle/)
* [Spring Cloud Data Flow Samples](https://docs.spring.io/spring-cloud-dataflow-samples/docs/current/reference/htmlsingle/) 

