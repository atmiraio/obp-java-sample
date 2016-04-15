# Introducción

Para el hackathon vamos a utilizar el API que provee [Open Bank Project](https://openbankproject.com) en su versión 2.0.0 que se encuentra actualmente en estado Draft, pero que propociona más recursos que la Stable.

## Autenticación

Para poder interactuar las aplicaciones de terceros con el API del *sandbox*, necesitan obtener las clave OAuth (consumer key y consumer secret) del servidor 
[OAuth Server 1.0](http://tools.ietf.org/html/rfc5849) preparado para la demo. Esto obliga a registar previamente la aplicación para obtener las anteriores en el siguiente [enlace](about:blank).  
*Hasta que no esté cerrado el entorno final, se pueden registrar las aplicaciones en el sandbox [aquí](https://apisandbox.openbankproject.com/consumer-registration)*  

Existen distintas librerías que dan soporte a este estándar, pero para el ejemplo que os proponemos, desarrollado en Java, la recomendación es utilizar [signpost](https://github.com/mttkay/signpost) en su versión 1.2.

### Proceso de login y obtención de token
Se utilizan los datos obtenidos previamente en el registro. En este caso no existe front, con lo que la petición espera la entrada por consola del código de verificación que nos proporciona el OAuth Server 

```java
       OAuthConsumer consumer = new CommonsHttpOAuthConsumer(
                "pcgozwsz3ny3owzsmmetfti54zxikmr1huwlcrpn",
                "bhj2jrfd3q5105yob1lbwguv1djsvb5ngtpt15lm");

        OAuthProvider provider = new CommonsHttpOAuthProvider(
                "https://apisandbox.openbankproject.com/oauth/initiate",
                "https://apisandbox.openbankproject.com/oauth/token",
                "https://apisandbox.openbankproject.com/oauth/authorize");
        System.out.println("Obteniendo el token de OBP...");

        // no hay soporte para callback
        String authUrl = provider.retrieveRequestToken(consumer,);

        System.out.println("Request token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());

        System.out.println("Ahora visita:\n" + authUrl
                + "\n... y consigue la autorización para esta aplicacion");
        System.out.println("Introduce el PIN code and y pulsa ENTER para continuar:");

        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String pin = br.readLine();

        System.out.println("Obtenoendo el access token de OBP...");

        provider.retrieveAccessToken(consumer, pin);

        System.out.println("Access token: " + consumer.getToken());
        System.out.println("Token secret: " + consumer.getTokenSecret());
```


### Ejemplo de una petición de consulta
Para ello hacemos un **GET** a https://apisandbox.openbankproject.com/obp/v2.0.0/my/accounts y de esta forma nos devuelve todas las cuentas que tiene  el usuario previamente autenticado.

```java
    @RequestMapping(value = "/accounts", method = RequestMethod.GET)
    public ModelAndView getAccounts(HttpServletRequest request) throws ApiCallFailedException{
        log.debug("Peticion de cuentas de usuario. URL:" + ACCOUNTS_URL);
        
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpGet requestGet = new HttpGet(ACCOUNTS_URL);
        ModelAndView model= null;
        
        try {
            consumer.sign(requestGet);
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            log.error("Error al firmar la peticion", e);
            throw new ApiCallFailedException(e.getMessage(), e);
        }
        org.apache.http.HttpResponse response = null;
        try {
            response = httpClient.execute(requestGet);
        } catch (IOException e) {
            log.error("Error al invocar al API", e);
            throw new ApiCallFailedException(e.getMessage(), e);
        }

        HttpEntity entity = response.getEntity();
        int statusCode = response.getStatusLine().getStatusCode();

        if (statusCode == 200 || statusCode == 201) {
             Account[] accounts;
            try {
                accounts = parseJsonResponse(entity);
            } catch (ParseException | IOException e) {
                log.error("Error al parsear la respuesta", e);
                throw new ApiCallFailedException(e.getMessage(), e);
            }
             model = new ModelAndView("accounts");
             model.addObject("accounts", accounts);
             return model;
        } else {
            log.error("Error en la respuesta de la petcion [" + statusCode + "]");
            throw new ApiCallFailedException("Error en la respuesta de la petcion [" + statusCode + "]");
        }
```


La salida de esta petición es un json con la siguiente forma

```json
accounts": [{ 
    "id": "5dHBvPFLLbnnBi2fOYOy",
    "label": null,
    "views_available": [{
                "id": "owner",
                "short_name": "Owner",
                "description": null,
                "is_public": false,
                "alias": "",
                "hide_metadata_if_alias_used": false,
                "can_see_transaction_this_bank_account": true,
                ...
            }
        ],
        "bank_id": "rbs"
    }]
```

*Cada view_available proporciona un tipo de acceso a los datos que el usuario desea exponer. Se explica en más detalle en la documentación de OBP*

### Ejemplo de una petición de comando
Se trata de realiza una transacción entre una de tus cuentas y una cuenta pública (destinada en ocasiones a ONG).  
Accederemos por POST al recurso https://apisandbox.openbankproject.com/obp/v2.0.0/banks/BANK_ID/accounts/ACCOUNT_ID/VIEW_ID/transaction-request-types/TRANSACTION_REQUEST_TYPE/transaction-requests


| Path params        |                                           |
|--------------------|-------------------------------------------|
| BANK_ID            | identificador del banco                   |
| ACCOUNT_ID         | identificador de la cudenta               |
| VIEW_ID            | OWNER                                     |
| TRANSACTION_REQUEST_TYPE | SANDBOX_TAN                         |


Ejemplo de petición

```json
{  
"to":{    
    "bank_id":"BANK_ID",    
    "account_id":"ACCOUNT_ID"  
    },  
    "value":{    
        "currency":"EUR",    
        "amount":"100.53"  
    },  
    "description": "A description for the transaction to be created",  
    "challenge_type":"one of the transaction types possible for the account"
    }
}
```

Código Java para la petición de **crear transaccción**

```java
    @RequestMapping(value = "/transactions", method = RequestMethod.POST)
    public String getTransactions(@ModelAttribute("transaction") Transaction transaction, BindingResult bindingResult,
            final Model model) throws ApiCallFailedException {
        String url = String.format(TRANSACTION_URL, BANK_ID, ACCOUNT_ID, VIEW_ID);
        HttpClient httpClient = HttpClientBuilder.create().build();
        HttpPost request = new HttpPost(url);

        StringEntity params = null;
        try {
            String jsonRequest = createJsonRequest(transaction);
            params = new StringEntity(jsonRequest);
        } catch (UnsupportedEncodingException | JsonProcessingException e) {
            log.error("Error al crear el JSON de la peticion", e);
            throw new ApiCallFailedException(e.getMessage(), e);
        }
        request.addHeader("content-type", "application/json");
        request.setEntity(params);
        try {
            consumer.sign(request);
        } catch (OAuthMessageSignerException | OAuthExpectationFailedException | OAuthCommunicationException e) {
            log.error("Error al firmar la peticion", e);
            throw new ApiCallFailedException(e.getMessage(), e);
        }

        //  TODO Finalizar con datos reales
        org.apache.http.HttpResponse response = null;
        try {
            response = httpClient.execute(request);
            HttpEntity entity = response.getEntity();
            String jsonString = EntityUtils.toString(entity);
            log.info("Salida de la peticion: " + jsonString);
        } catch (IOException e) {
            log.error("Error al realizar la peticion", e);
            throw new ApiCallFailedException(e.getMessage(), e);
        }
```

## Datos de prueba

### Usuario

| API Key credentials|                                           |
|--------------------|-------------------------------------------|
| USER               | joe.bloggs@example.com                    |
| PASSWORD           | qwerty                                    |

### Open Bank API register

|                    |                                           |
|--------------------|-------------------------------------------|
|Application Type    |  web                                      |
|Application Name    |  hackxxx                                  |
|Developer Email     |  hackxxx@mailinator.com                   |
|App Description     |  App de pruebas para el hackathon         |
|Consumer Key        |  pcgozwsz3ny3owzsmmetfti54zxikmr1huwlcrpn |
|Consumer Secret     |  bhj2jrfd3q5105yob1lbwguv1djsvb5ngtpt15lm |

### Cuenta de prueba

|                    |                                           |
|--------------------|-------------------------------------------|
|AccountId           |  hackxxx-account                          |
|BankId              |  rbs                                      |

### Juego de datos
Podemos ver los datos que provee el *sandbox* temporal accediendo a la aplicación de pruebas [Social Finance](https://sofisandbox.openbankproject.com/index).  
Cuando tengamos los datos finales los subiremos al repositorio para que podáis realizar soluciones más eficaces, teniendo en cuenta el modelo final.

## Documentación y API Explorer

 - [Documentación de OBP](https://github.com/OpenBankProject/OBP-API/wiki/)
 - [API Explorer v2.0.0](https://apiexplorersandbox.openbankproject.com/?version=2.0.0&list-all-banks=false&core=&psd2=&obwg=&ignoredefcat=)



# Ejemplo

La aplicación muestra una lista de las cuentas del usuario,previamente autorizado, y permite realizar una transacción simple a otra cuenta.  
Está basada en Spring Boot, para facilitar el arranque sin necesidad de instalar entornos de ejecución.

## Configuración

Debemos **registrar nuestra aplicación**, como se ha descrito previamente, y **crear un cliente de prueba** en el [enlace](https://apisandbox.openbankproject.com/user_mgt/sign_up) (de momento para esta versión del *sandbox*)

#### OauthConf
En el Config Bean com.atmira.obp.config.OAuthConfig se deben modificar los valores con los que se obtienen al dar de alta la aplicación.

```java
return new CommonsHttpOAuthConsumer(
        Consumer Key,
        Consumer Secret);
```

*Temporalmente utilizaremos el *sandbox* por defecto https://apisandbox.openbankproject.com, hasta que se configura el que estará disponible para el *sandbox**

```java
return new CommonsHttpOAuthProvider(
        "https://apisandbox.openbankproject.com/oauth/initiate",
        "https://apisandbox.openbankproject.com/oauth/token",
        "https://apisandbox.openbankproject.com/oauth/authorize");
```

#### Hostname
Para mantener los datos para realizar la firma de las peticiones, es necesario que el callback se haga sobre el mismo hostname (127.0.0.1:8080) que ejecuta la aplación. No será válido si levantamos la aplicación como localhost:8080 y el callback se hace a 127.0.0.1:8080, porque perderíamos los datos de la sesión.

*El valor de callback está como constante *CALLBACK_URL* en `com.atmira.obp.controllers.LoginController`.*

#### Valores de prueba

Los datos de la cuenta asociada al usuario de prueba están en com.atmira.obp.controllers.TransactionController` y se tendrán que modificar si se utilizan otro distinto del que se proporciona como ejemplo.

```java
    private final String BANK_ID = "rbs";
    private final String ACCOUNT_ID = "hackxxx-account";
    private final String VIEW_ID = "owner";
    private final String CHALLENGE_TYPE = "SANDBOX_TAN";
    private final String CURRENCY = "EUR";
```

## Requisitos
Para ejecutar el ejemplo, necesitas tener previamente instalado [java](http://java.com/) en su versión 8 y [maven 3](http://www.maven.com).

## Descarga y Ejecución
La forma más fácil comenzar es descargar el proyecto y ejecutarlo directamente desde la línea de comandos.

```bash
$ git clone https://github.com/atmiradigital/obp-java-sample.git
$ cd obp-java-sample
$ mvn spring-boot:run
```

Una vez levantada la aplicación, accederemos a la url http://127.0.0.1:8080 y comenzaremos con el proceso de login, consulta, transacción.


## AVISO

El entorno actual no es el definitivo, pero puede ser muy útil para comenzar a expirimentar sobre la plataforma. 
En el ejemplo que tenéis en este repositorio, existen casos que todavía no se han podido implementar completamente esperando al cierre tanto de la plataforma, como del juego de datos. En el momento que tengamos completos todos estos requisitos, iremos subiendo las versiones ya cerradas. 

