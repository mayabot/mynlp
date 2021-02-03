package com.mayabot.nlp

import com.mayabot.nlp.common.injector.*
import org.junit.Assert.assertTrue
import org.junit.Test

class InjectTest {


    @Test
    fun testSingleton() {
        val injector = Injector.create(listOf(
                createModule {
                    bind(UserDao::class.java).toClass(UserDaoImpl::class.java)
                }
        ))
        val dao: UserDao? = injector.getInstance()!!
        val dao2: UserDao? = injector.getInstance()!!

        assertTrue(dao === dao2)
    }

    @Test
    fun testSingleton2() {
        val injector = Injector.create()
        val dao: UserDao = injector.getInstance()!!
        val dao2: UserDao = injector.getInstance(UserDaoImpl::class.java)!!

        println(dao)
        println(dao2)
        assertTrue(dao === dao2)
    }

    @Test
    fun test() {

        val models = ArrayList<Module>()

        models += object : AbstractModule() {
            override fun configure() {
                bind(DbService::class.java).toInstance(MysqlService())
                bind(BissService::class.java).toClass(BissServiceImpl::class.java)
            }
        }

        val injector = Injector.create(models)

        val controller = injector.getInstance(SprintUIController::class.java)!!

        controller.hi()

    }

}

@ImplementedBy(UserDaoImpl::class)
interface UserDao

@Singleton
class UserDaoImpl : UserDao


interface DbService {
}

class MysqlService : DbService {

}

@ImplementedBy(ElasticSearchService::class)
interface SearchService

class ElasticSearchService : SearchService {

}

interface BissService {
    fun showService()
}

@Singleton
class BissServiceImpl(
        val dbService: DbService,
        val searchService: SearchService
) : BissService {
    override fun showService() {
        println(dbService)
        println(searchService)
    }

}

@Singleton
class SprintUIController(val bissService: BissService) {
    fun hi() {
        bissService.showService()
    }
}


