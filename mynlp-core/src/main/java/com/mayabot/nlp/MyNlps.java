/*
 *  Copyright 2017 mayabot.com authors. All rights reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mayabot.nlp;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.inject.*;
import com.google.inject.matcher.AbstractMatcher;
import com.google.inject.spi.InjectionListener;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import com.mayabot.nlp.logging.InternalLogger;
import com.mayabot.nlp.logging.InternalLoggerFactory;
import com.mayabot.nlp.resources.MynlpResourceFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class MyNlps {


    static InternalLogger logger = InternalLoggerFactory.getInstance(MyNlps.class);

    private static final Injector injector;

    static {
        injector = createInject();
    }

    private static Injector createInject() {

        Settings settings = Settings.build();

        Environment environment = new Environment(settings);


        ArrayList<Module> modules = Lists.newArrayList();

        modules.add(new AbstractModule() {
            @Override
            protected void configure() {

                bind(Settings.class).toInstance(settings);
                bind(Environment.class).toInstance(environment);
                bind(MynlpResourceFactory.class).toInstance(environment.getMynlpResourceFactory());

                //process initialize interface
                bindListener(new AbstractMatcher<TypeLiteral<?>>() {
                    @Override
                    public boolean matches(TypeLiteral<?> t) {
                        return InitInterface.class.isAssignableFrom(t.getRawType());
                    }
                }, new TypeListener() {
                    @SuppressWarnings({"unchecked", "rawtypes"})
                    @Override
                    public <I> void hear(TypeLiteral<I> type, TypeEncounter<I> encounter) {
                        encounter.register((InjectionListener) injectee -> {
                            InitInterface initInterface = (InitInterface) injectee;
                            try {
                                initInterface.init();
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        });
                    }
                });

            }
        });


        //加载模块，在配置文件中声明的
        modules.addAll(loadModules(environment));

        Injector injector = Guice.createInjector(modules);

        return injector;
    }

    private static List<Module> loadModules(Environment environment) {
        try {

            Set<String> set = Sets.newHashSet();
            Enumeration<URL> resources = MyNlps.class.getClassLoader().getResources("META-INF/mynlp.factories");

            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();

                logger.info("Found mynlp.factories {}", url.toString());

                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream(), "utf-8"));

                String line = reader.readLine();

                while (line != null) {

                    set.add(line);

                    line = reader.readLine();
                }


                reader.close();
            }

            return set.stream().filter(line -> !line.trim().isEmpty()).map(clazzName -> {
                try {
                    Class<? extends Module> clazz = (Class<? extends Module>) Class.forName(clazzName.trim());

                    try {
                        Constructor<? extends Module> c1 = clazz.getConstructor(Environment.class);
                        if (c1 != null) {
                            return c1.newInstance(environment);
                        }
                    } catch (NoSuchMethodException e) {

                    }

                    //call default con
                    Module module = clazz.newInstance();


                    return module;
                } catch (ClassNotFoundException e) {
                    System.err.println("Not Found Class " + clazzName);
                    throw new RuntimeException(e);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }).collect(Collectors.toList());

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }

    public static Injector getInjector() {
        return injector;
    }

    public static void injectMembers(Object instance) {
        injector.injectMembers(instance);
    }

    public static <T> T getInstance(Key<T> key) {
        return injector.getInstance(key);
    }

    public static <T> T getInstance(Class<T> type) {
        return injector.getInstance(type);
    }


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
//        System.out.println(injector.getInstance(D.class));
//
//        injector.getAllBindings().keySet().forEach(System.out::println);
//    }
}
