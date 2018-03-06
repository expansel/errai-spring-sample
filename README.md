# errai-spring-sample

This is a sample project to illustrate integrating Errai with Spring in a non-springboot webapp using the errai-spring-server library <https://github.com/expansel/errai-spring-server>.

The main point of interest is the code, but the user interface can help seeing the flow of a specific piece of functionality.

## Compiling and running
Make sure nothing else is running on port 8765.

```shell
mvn clean package tomcat7:run
```

## Login to UI
After compiling and running, the web application will can be accessed at this url: <http://localhost:8765/errai-spring-sample/login>

You will first be presented with a login page and you can use the following users.

User role:
* username: user
* password: 11

Admin role:
* username: admin
* password: 11

After login there will be buttons that illustrate the different Errai @Service implementations like MessageCallback, @Command, RPC and initiating named messages sent back to the client side. Many of them just result in a log statement.

## Message Bus and JAX-RS
Illustrates:
*   RPC services
*   MessageCallback implementation
*   @Command
*   Injection of messsage bus onto spring beans.
*   Injection of other beans into Errai Services 

There is also an example of using a client side JAX-RS interface to call a Spring web mvc service. A JAX-RS server side sample has not been added yet. 

## Security Annotations
Only the admin user role has privileges on the buttons, and most of them illustrate the use of different annotations available.

*   Errai's @RestrictedAccess with plain strings: `@RestrictedAccess(roles = { "admin" })`
*   @RestrictedAccess with a RequiredRolesProvider: `@RestrictedAccess(providers=AdminRequiredRolesProvider.class)`
*   Spring's @Secured: `@Secured("admin")`
*   Spring's @PreAuthorize: `@PreAuthorize("hasAuthority('admin')")`
*   JSR250 @RolesAllowed: `@RolesAllowed({ "admin" })`

Most are applied directly to bus services in the sample, but there is also one on a class called SpringService which is a 
service class wired in by Spring to a MessageBus service.

## Structural notes:
*  There are lots of javadoc comments explaining different parts, so the source is where it's at.
*  The MessageBus is secured by the Spring security filter and configured to enforce bus access to authenticated users. Therefore 
   there are special AuthenticationEntryPoint implementation classes to deal with appropriate MessageBus and JAX-RS clients. See the 
   `ErraiClientBusAuthenticationEntryPoint` and `ErraiRestClientAuthenticationEntryPoint` classes, which are configured to  
   avoid the ajax calls being redirected to the normal login page and get an html instead of json response. 
   side being redirected by spring to the base login page
*  A `web.xml` file was used in preference to pure java in order to specify the `metadata-complete="true"` attribute. This 
   prevents Errai JEE annotations for things like filters to be automatically applied. The only way to turn off such a filter is to
   put a bogus filter mapping and this I found preferable.
*  It follows the Spring recommended approach of separate contexts for the root context and the web context. The 
   `contextConfigLocation` in web.xml references `@Configuration` annotated classes in 
   `com.expansel.errai.spring.sample.config` package.
*  @ComponentScan was avoided in favour of explicit java config to make clear the different objects in use and how they are related, 
   without the background magic of too much autowiring. A few classes here and there might have some dependencies autowired.
*  The DefaultBlockingServlet was subclasses to fix a problem where a certain error did not have the Content-Type set to json.
*  A separate MethodSecurity config is imported into both the root and dispatcher context to support the spring security annotations. 
   In practice one would probably prefer a specific annotation style, but in this case support for all were enabled for illustrative 
   purposes. The separate MethodSecurity config class was also used to enable setting the RoleVoter prefix. 
*  @RestrictedAccess is processed by an AOP proxy using the AspectJ annotations applied by Spring.
*  A custom SecurityExceptionHandler (this is a new feature in Errai) was implemented to avoid Errai Navigation and rather just
   display popup alerts to show what exceptions are being processed on the client side.


## Missing and TODO:
*  @RestrictedAccess on Spring controller classes. I don't recommend doing this but there is some commented code that reference how 
   it is possible to do.
*  No JAX-RS server side implementation samples
*  CSRF is disabled, but hope to add this later
*  Want to also still illustrate and test WebSocket support with the message bus


