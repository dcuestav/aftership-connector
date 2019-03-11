#Aftership connector
### Descripción
Este proyecto recibe las notificaciones que Aftership envía a través de webhooks y se comunica con el API de Prestashop para marcar el pedido como "Entregado".

### Despliegue
Se despliega en Azure Cloud Functions (serverless) con el comando
```
mvn azure-functions:deploy
```
previamente hay que tener instalado Azure cli y loggearse con el comando
```
az login
``` 
### Variables de entorno
La aplicación necesita las siguiente variables de entorno
- _PRESTAKEY_ - Clave de webservice de Prestashop con el permiso _PUT order_histories_
- _PRESTAURL_ - Ej. https://www.mipresta.com/api

### Requisitos en Aftership
Para hacer la actualización de estado del pedido hace falta obetener el **order_id** numérico del mensaje de Aftership. En primer lugar se consulta el campo _order_id_. Si este campo no es numérico se obtiene del campo _order_id_path_ que tiene esta forma:
```
https://www.mipresta.com/es/index.php?controller=order-detail&id_order=12191
```

### Requisitos en Prestashop
El nuevo estado que se crea en Prestashop para cada pedido es el de id 5, que inicialmente corresponde al estado "Entregado".