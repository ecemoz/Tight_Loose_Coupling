package com.loose.coupling;

import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

public class LooseCouplingExample {
    public static void main(String[] args) {

        ApplicationContext applicationContext = new ClassPathXmlApplicationContext("applicationIoCLooseCouplingExample.xml");
        UserManager userManagerwithDB = applicationContext.getBean("userManagerWithUserDataProvider", UserManager.class);

        UserManager userManagerwithWebService = applicationContext.getBean("userManagerWithWebServiceDataProvider", UserManager.class);

//        UserDataProvider dataProvider = new UserDatabaseProvider();
//        UserManager userManagerwithDB =  new UserManager(dataProvider);
        System.out.println(userManagerwithDB.getUserInfo());

//        UserDataProvider webServiceProvider = new WebServiceDataProvider();
//        UserManager userManagerwithWebService = new UserManager(webServiceProvider);
        System.out.println(userManagerwithWebService.getUserInfo());
    }
}