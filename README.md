# 예외처리

서블릿은 2가지 방식을 지원

1) exception

2) response.sendError(HTTP 상태코드, 오류메시지)

## Exception

**자바 직접 실행**

자바의 메인 메서드를 직접 실행하는 경우 main이라는 이름의 쓰레드가 실행된다.

실행도중에 예외를 잡지 못하고 처음 실행한 메인메서드를 넘어서 예외가 던져지면, 예외 정보를 남기고 해당 쓰레드는 종료된다.

**웹 어플리케이션**

웹 애플리케이션은 사용자 요청별로 별도의 쓰레드가 할당되고, 서블릿 컨테이너 안에서 실행된다.

애플리케이션에서 예외가 발생했는데, 어디선가 try ~catch로 예외를 잡아서 처리하면 아무런 문제가 없다.

그런데 애플리케이션에서 예외를 잡지 못하고, 서블릿 밖으로 까지 예외가 전달되면 어뗗게 동작할까?

WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)

결국 톰캣 같은 WAS 까지 예외가 전달된다. WAS는 예외가 올라오면 어떻게 처리해야 할까?

**스프링부트가 기본 제공하는 예외페이지**

Whitelabel Error Page 라고 뜨는 것.

```properties
server.error.whitelabel.enabled=false #기본페이지 옵션 끄기, default가 true
```

위의 옵션을 끄고 실행하면 `에외가 터졌을 때` HTTP Status 500 – Internal Server Error가 발생한다. (tomcat(was)이 기본으로 제공하는 오류 화면을 볼 수 있다)

`Exception` 의 경우 서버 내부에서 처리할 수 없는 오류가 발생한 것으로 생각해서 HTTP 상태 코드 500을 반환한
다.

## response.sendError

오류가 발생했을 때 `HttpServletResponse` 가 제공하는 `sendError` 라는 메서드를 사용해도 된다.

이것을 호출 한다고 당장 예외가 발생하는 것은 아니지만, 서블릿 컨테이너에게 오류가 발생했다는 점을 전달할 수 있다.

이 메서드를 사용하면 HTTP 상태 코드와 오류 메시지도 추가할 수 있다.

`response.sendError(HTTP 상태 코드)`

`response.sendError(HTTP 상태 코드, 오류 메시지)`

**sendError 흐름**

```text
WAS(sendError 호출 기록 확인) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러 (response.sendError())
```

`response.sendError()` 를 호출하면 `response` 내부에는 오류가 발생했다는 상태를 저장해둔다.

그리고 서블릿 컨테이너는 고객에게 응답 전에 `response` 에 `sendError()` 가 호출되었는지 확인한다.

그리고 호출 되었다면 설정한 오류 코드에 맞추어 기본 오류 페이지를 보여준다.

## 서블릿 예외 처리 - 오류 페이지 작동 원리

예를 들어서 `RuntimeException` 예외가 WAS까지 전달되면, WAS는 오류 페이지 정보를 확인한다.

확인해보니 `RuntimeException` 의 오류 페이지로 `/error-page/500` 이 지정되어 있다.

WAS는 오류 페이지를 출력하기 위해 `/error-page/500` 를 다시 요청한다.

**오류 페이지 요청 흐름**

```
WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/500) -> View
```

**예외 발생과 오류 페이지 요청 흐름**

```
1.WAS(여기까지 전파) <- 필터 <- 서블릿 <- 인터셉터 <- 컨트롤러(예외발생)

2.WAS `/error-page/500` 다시 요청 -> 필터 -> 서블릿 -> 인터셉터 -> 컨트롤러(/error-page/
500) -> View
```

**중요한 점은 웹 브라우저(클라이언트)는 서버 내부에서 이런 일이 일어나는지 전혀 모른다는 점이다. 오직 서버 내부에 서 오류 페이지를 찾기 위해 추가적인 호출을 한다.**

정리하면 다음과 같다.

1. 예외가 발생해서 WAS까지 전파된다.

2. WAS는 오류 페이지 경로를 찾아서 내부에서 오류 페이지를 호출한다. 이때 오류 페이지 경로로 필터, 서블릿, 인터셉터, 컨트롤러가 모두 다시 호출된다.

즉, 오류가 발생하면 클라이언트는 1번 호출했으나 실제로는 2번 호출한 효과를 나타냄

### 오류 정보

**request.attribute에 서버가 담아준 정보**

`javax.servlet.error.exception` : 예외

`javax.servlet.error.exception_type` : 예외 타입

`javax.servlet.error.message` : 오류 메시지

`javax.servlet.error.request_uri` : 클라이언트 요청 URI

`javax.servlet.error.servlet_name` : 오류가 발생한 서블릿 이름

`javax.servlet.error.status_code` : HTTP 상태 코드

## 서블릿 예외 처리 - 필터

오류가 발생하면 오류 페이지를 호출하기 위해 WAS 내부에서 다시 한번 호출한다.

인터셉터 또한 다시 호출된다. 이건 정말 비효율적이다. 오류 페이지 호출한다고 다시 호출하다니...

결국 클라이언트로 부터 발생한 정상 요청인지, 아니면 오류 페이지를 출력하기 위한 내부 요청인지 구분을 위해 `DispatcherType` 이라는 추가 정보를 제공한다.

## DispatcherType

`log.info("dispatchType={}", request.getDispatcherType())`

출력해보면 오류 페이지에서 `dispatchType=ERROR` 로 나오는 것을 확인할 수 있다.

고객이 처음 요청하면 `dispatcherType=REQUEST` 이다.

이렇듯 서블릿 스펙은 실제 고객이 요청한 것인지, 서버가 내부에서 오류 페이지를 요청하는 것인지
`DispatcherType` 으로 구분할 수 있는 방법을 제공한다.

`javax.servlet.DispatcherType`

```
 public enum DispatcherType {
     FORWARD,
     INCLUDE,
     REQUEST,
     ASYNC,
     ERROR
}
```

**DispatcherType**

`REQUEST` : 클라이언트 요청

`ERROR` : 오류 요청

`FORWARD` : MVC에서 배웠던 서블릿에서 다른 서블릿이나 JSP를 호출할 때 `RequestDispatcher.forward(request, response);`

`INCLUDE` : 서블릿에서 다른 서블릿이나 JSP의 결과를 포함할 때 `RequestDispatcher.include(request, response);`

`ASYNC` : 서블릿 비동기 호출

```java

@Bean
public FilterRegistrationBean logFilter() {
    FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
    filterRegistrationBean.setFilter(new LogFilter());
    filterRegistrationBean.setOrder(1);
    filterRegistrationBean.addUrlPatterns("/*");
    filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);
    return filterRegistrationBean;
}
```

`filterRegistrationBean.setDispatcherTypes(DispatcherType.REQUEST, DispatcherType.ERROR);`

이렇게 두 가지를 모두 넣으면 클라이언트 요청은 물론이고, 오류 페이지 요청에서도 필터가 호출된다.

아무것도 넣지 않으면 기본 값이 `DispatcherType.REQUEST` 이다. 즉 클라이언트의 요청이 있는 경우에만 필터가 적용된다.

특별히 오류 페이지 경로도 필터를 적용할 것이 아니면, 기본 값을 그대로 사용하면 된다.

물론 오류 페이지 요청 전용 필터를 적용하고 싶으면 `DispatcherType.ERROR` 만 지정하면 된다.














