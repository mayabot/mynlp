/*
 * Copyright 2018 mayabot.com authors. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.mayabot.nlp;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.Singleton;
import com.google.inject.name.Names;
import org.junit.Test;

public class GuiceTest {
    @Singleton
    public static class A {
        private String name = "default";

        public A set(String name) {
            this.name = name;
            return this;
        }

        @Override
        public String toString() {
            return name + "  " + super.toString();
        }
    }

    public static void main(String[] args) {
        Injector inject = Guice.createInjector(binder -> {
            binder.bind(A.class).toInstance(new A().set("p"));
        });
        Injector c1 = inject.createChildInjector(binder -> {
            binder.bind(A.class).annotatedWith(Names.named("a")).toInstance(new A());
        });
        Injector c2 = inject.createChildInjector();
        System.out.println(c1.getInstance(Key.get(A.class, Names.named("a"))));
        System.out.println(c2.getInstance(A.class));
    }

    @Test
    public void test() {


        //    @Singleton
//    public static class A{
//
//    }
//
//    @Singleton
//    public static class B{
//        @Inject
//        public B(A a){
//            System.out.println(a);
//        }
//    }
//
//    @Singleton
//    public static class C{
//        @Inject
//        public C(B b){
//            System.out.println(b);
//        }
//    }
//
//    @Singleton
//    public static class D{
//        @Inject
//        public D(B b,C c){
//            System.out.println(b);
//        }
//    }
//
//    public static interface F{
//
//    }
//
//    public static class G implements F{
//        public G(){
//
//        }
//    } public static class X implements F{
//        public X(){
//
//        }
//    }
//
//    public static void main(String[] args) {
//        Injector injector = Guice.createInjector(binder -> {
//            binder.bind(F.class).to(G.class);
//        });
//
//        System.out.println(injector.instanceOf(D.class));
//
//        injector.getAllBindings().keySet().forEach(System.out::println);
//    }
    }
}
