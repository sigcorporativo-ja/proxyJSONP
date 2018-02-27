package es.juntadeandalucia.mapea.builder;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;
import org.json.JSONObject;


public class JSBuilder {



   /**
    * Wraps the javascript code to execute it as parameter of the specified function
    * 
    * @param code the javascript code to execute it as parameter
    * 
    * @param callbackFn the name of the javascript
    * function to execute as callback
    * 
    * @return the execution of the callback with the javascript code as parameter
    */
   public static void wrapCallback (StringBuilder code, String callbackFn) {
      // if no callback function was specified do not wrap the code
      if (!StringUtils.isEmpty(callbackFn)) {
         code.insert(0, "(").insert(0, callbackFn);
         code.append(");");
      }
   }

   /**
    * Wraps the JSON array to execute it as parameter of the specified function
    * 
    * @param jsonArray the JSON array to execute it as parameter
    * 
    * @param callbackFn the name of the javascript
    * function to execute as callback
    * 
    * @return the execution of the callback with the JSON array as parameter
    */
   public static String wrapCallback (JSONArray jsonArray, String callbackFn) {
      return wrapCallback(jsonArray.toString(), callbackFn);
   }
   
   /**
    * Wraps the JSON to execute it as parameter of the specified function
    * 
    * @param json the JSON to execute it as parameter
    * 
    * @param callbackFn the name of the javascript
    * function to execute as callback
    * 
    * @return the execution of the callback with the JSON as parameter
    */
   public static String wrapCallback (JSONObject json, String callbackFn) {
      return wrapCallback(json.toString(), callbackFn);
   }
   
   /**
    * Wraps the javascript code to execute it as parameter of the specified function
    * 
    * @param code the javascript code to execute it as parameter
    * 
    * @param callbackFn the name of the javascript
    * function to execute as callback
    * 
    * @return the execution of the callback with the javascript code as parameter
    */
   public static String wrapCallback (String code, String callbackFn) {
      String wrappedCode = code;
      // if no callback function was specified do not wrap the code
      if (!StringUtils.isEmpty(callbackFn)) {
         StringBuilder wrapBuilder = new StringBuilder();
         wrapBuilder.append(callbackFn).append("(").append(code).append(");");
         wrappedCode = wrapBuilder.toString();
      }
      return wrappedCode;
   }
}
