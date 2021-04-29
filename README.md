# Protobuf http client

- [背景](#背景)
- [安装](#安装)
- [使用说明](#使用说明)
- [示例](#示例)

## 背景

`Protobuf http client` 最开始因为在公司内部存在跨语言Java与C++应用交互，C++服务因为性能考量使用protobuf作为序列化方式，为此多个使用Java的团队各自实现了交互。
为了统一管理维护，收集各方需求后实现了一个基于接口注解方式实现Http+Protobuf交互的client代理。

> 由于不同公司使用命名服务不一样，为了实现简单，这个工程使用nacos作为配置中心与注册中心

这个仓库的目标是：

1. 使用nacos作为配置中心与注册中心。
2. 基于接口注解订阅下游服务实例。
3. 基于接口注解方式生成Http+Protobuf client代理。
4. 基于接口注解实现负载均衡策略

## 安装

```sh
 clone https://github.com/silent-field/protobuf-http-client.git
```

## 使用说明

订阅服务注解`ProtobufHttpClientMeta`
* serviceName：需要订阅的服务名，注册中心上对应的标识，根据服务名从注册中心订阅服务实例列表
* desc：服务描述
* lrRule：路由策略，默认是轮询

```
/**
 * 服务名
 */
String serviceName();

/**
 * 服务描述
 */
String desc() default "";

/**
 * 路由策略
 * @return
 */
LoadBalanceRuleEnum lrRule() default LoadBalanceRuleEnum.ROUND;
```

服务接口注解`ProtobufHttpRestfulMeta`
* path：接口uri
* desc：接口描述

```
/**
 * 接口路径
 */
String path() ;

/**
 * 接口描述
 */
String desc() default "";
```

## 示例

1. 服务端提供接受protobuf入参的echo服务
```java
@Controller
@RequestMapping(value = "/test")
public class TestController {
	@RequestMapping(value = "/echo1", method = RequestMethod.POST, produces = "application/x-protobuf")
	@ResponseBody
	public Echo.EchoResponse echo1(@RequestBody Echo.EchoRequest request) {
		Echo.EchoResponse echoResp = Echo.EchoResponse.newBuilder()
				.setMessage(request.getMessage() + "-" + ThreadLocalRandom.current().nextLong()).build();

		return echoResp;
	}

	@RequestMapping(value = "/echo2", method = RequestMethod.POST, produces = "application/x-protobuf")
	@ResponseBody
	public Echo.EchoResponse echo2(@RequestBody Echo.EchoRequest request) {
		Echo.EchoResponse echoResp = Echo.EchoResponse.newBuilder()
				.setMessage(request.getMessage() + "-" + ThreadLocalRandom.current().nextLong()).build();

		return echoResp;
	}
}
```

2. 客户端定义protobuf http client接口，并使用ProtobufHttpClientMeta、ProtobufHttpRestfulMeta注解

```java
@ProtobufHttpClientMeta(serviceName = "phc-test1", desc = "测试服务1")
public interface HttpClientService {
	@ProtobufHttpRestfulMeta(desc = "测试方法1", path = "/test/echo1")
	Response echo1(Message request);

	@ProtobufHttpRestfulMeta(desc = "测试方法2", path = "/test/echo2")
	Response echo2(Message request);
}
```

3. 使用spring注入调用echo服务
```java
@Slf4j
@Component
public class TestSender {
    @Autowired
    private HttpClientService httpClientService;

    @PostConstruct
    private void init() {
        new Timer().scheduleAtFixedRate(new TimerTask() {
            public void run() {
                try {
                    call();
                } catch (Exception e) {
                    log.error("",e);
                }
            }
        } , 1000 , 3000);
    }

    private void call() throws Exception{
        Echo.EchoRequest echo =  Echo.EchoRequest.newBuilder().setMessage("echo1 test request").build();
        Response response = httpClientService.echo1(echo);
        byte[] bytes = response.getResponseBodyAsBytes();
        System.out.println(Echo.EchoResponse.parseFrom(bytes));

        // -----------------
        echo =  Echo.EchoRequest.newBuilder().setMessage("echo2 test request").build();
        response = httpClientService.echo2(echo);
        bytes = response.getResponseBodyAsBytes();
        System.out.println(Echo.EchoResponse.parseFrom(bytes));
    }
}
```
