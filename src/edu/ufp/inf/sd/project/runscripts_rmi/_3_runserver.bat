call setenv server

@cd %ABSPATH2CLASSES%
@cls
java -cp %CLASSPATH% -Djava.rmi.server.codebase=%SERVER_CODEBASE% -Djava.rmi.server.hostname=%SERVER_RMI_HOST% -Djava.security.policy=%SERVER_SECURITY_POLICY% %JAVAPACKAGEROLE%.JobShopServer %REGISTRY_HOST% %REGISTRY_PORT% %SERVICE_NAME_ON_REGISTRY%

@cd %ABSPATH2SRC%\%JAVASCRIPTSPATH%