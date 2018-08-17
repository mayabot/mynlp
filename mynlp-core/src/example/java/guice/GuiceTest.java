package guice;

import com.google.inject.Guice;
import com.google.inject.Injector;

import javax.inject.Singleton;

public class GuiceTest {
    public static void main(String[] args) {

        Injector injector = Guice.createInjector(new BillingModule());

        System.out.println(injector.getInstance(A.class).hashCode());
        System.out.println(injector.getInstance(A.class).hashCode());
    }

    @Singleton
    static class A {

    }
}
