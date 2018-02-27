# ProxyJSONP v2.0.0
ProxyJSONP es una distribución independiende del proxyJSONP que contiene Mapea4, para facilitar su despligue en entornos que lo requieran.
> :bulb: <a> Puedes consultar la [documentación de Mapea](https://github.com/sigcorporativo-ja/Mapea4/wiki/Proxys) sobre el uso de proxys </a>

## Requisitos
* jdk7.
* Maven.
* Debe desplegarse en servidor de aplicaciones.

## Generación war
```
mvn clean package
```

## Uso
Una vez desplegado, para hacer uso del proxy únicamente es necesario realizar una llamada al mismo, indicándole la url del recurso que queremos consumir y si lo deseamos en formato json o no, así como la función de callback.
```
http://[CONTEXTO_DESPLIEGUE]/proxy?url=[url_datos_origen]&tojson=[true|false]&callback=[funcion_de_calback]
```
