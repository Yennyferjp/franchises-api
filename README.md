como lanzar el docker 

Para lanzar el contenedor de Docker utilizando el archivo `compose.yaml`, sigue estos pasos:
1. Asegúrate de tener Docker y Docker Compose instalados en tu máquina.
2. Navega al directorio donde se encuentra el archivo `compose.yaml`.
3. Ejecuta el siguiente comando para iniciar los servicios definidos en el archivo:
   ```bash
   docker-compose -f compose.yaml up -d
   ```
   El flag `-d` ejecuta los contenedores en segundo plano (detached mode).
4. Verifica que los contenedores estén corriendo con el comando:
   ```bash
   docker ps
   ```     
5. Para detener los contenedores, puedes usar el siguiente comando:
   ```bash
   docker-compose -f compose.yaml down
   ``` 
