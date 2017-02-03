# ProxyJSONP
ProxyJSONP es una distribución independiende del proxyJSONP que contiene Mapea4, para facilitar su despligue en entornos que lo requieran.
> :bulb: <a> Puedes consultar la [documentación de Mapea](https://github.com/sigcorporativo-ja/Mapea4/wiki/Proxys) sobre el uso de proxys</a>
## Requisitos
Es necesario el uso de la especificación servlet 3 o superior, por lo que debe desplegarse en servidores de aplicaciones que soporten dicha especificación, como por ejemplo:
* Tomcat 7 o superior
* JBOSS 6 o superior 
## Uso
Una vez desplegado, para hacer uso del proxy únicamente es necesario realizar una llamada al mismo, indicándole la url del recurso que queremos consumir y si lo deseamos en formato json o no, así como la función de callback.

http://[CONTEXTO_DESPLIEGUE]/proxyJSONP?url=[url_datos_origen]&tojson=[true|false]&callback=[funcion_de_calback]

#### Consumo de servicios securizados
El proxyJSONP añade la posibilidad de consumir servicios securizados de Geoserver mediante la generación de tokens. Para ello es necesario importar la librería [ticket-0.0.1.jar](http://www.juntadeandalucia.es/madeja/repositoriodelibrerias/ja-external/es/guadaltel/framework/ticket/0.0.1/) en el proyecto y generar el token que se le pasará al proxy a través del parámetro _ticket_

Generación de ticket:
```java
java.util.Map<String, String> props = new java.util.HashMap<String, String>();
props.put("user", $usuario);
props.put("pass", $contraseña);
Ticket ticket = TicketFactory.createInstance();
String ticketStr = ticket.getTicket(props);
```
Llamada al proxy con el ticket generado:
```
http://[CONTEXTO_DESPLIEGUE]/proxyJSONP?url=[url_servicio_securizado]&tojson=[true|false]&callback=[funcion_de_calback]&ticket=[ticketStr]
```
Configuración en Mapea4JS:
```javascript
M.config('PROXY_URL', 'http://[CONTEXTO_DESPLIEGUE]/proxyJSONP=<%=ticketStr%>');
```
