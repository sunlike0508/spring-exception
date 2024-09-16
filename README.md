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


