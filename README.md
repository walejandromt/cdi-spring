# CDI-Spring bridge

[![Build Status](https://travis-ci.org/walejandromt/cdi-spring.svg?branch=master)](https://travis-ci.org/walejandromt/cdi-spring)

CDI Portable Extension + Spring Bean Puente(Bridge) Spring 5.0.0

### Bridge for get context of the Spring Framework.

# Goal

Be able to reuse spring beans in CDI application Extension.

# Usage

You have to use this CDI Portable Extension.

## Create a specific application context

Create a classpath resource called /spring/Spring-All-Module.xml and import your file configuration of the spring app
in this file.

## Example for the Spring-All-Module.xml in the classpath resource for you application

```xml
    <?xml version="1.0" encoding="UTF-8"?>
    <beans xmlns="http://www.springframework.org/schema/beans"
      xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
      xsi:schemaLocation="http://www.springframework.org/schema/beans
      http://www.springframework.org/schema/beans/spring-beans.xsd">

      <!-- Importar configuraciones de Spring -->
      <import resource="classpath:/spring/spring-cdi-mvc.xml"/>

    </beans>
```

## Inject your spring beans in your CDI beans

```java
    @Inject @InjectSpring(name="spring1")
    private SomeSpringStuff bean;
```
    
