package com.mayabot.nlp

import com.mayabot.nlp.common.injector.*


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


fun main() {

    val models = ArrayList<Module>()

    models += object : AbstractModule() {
        override fun configure() {
            bind(DbService::class.java).toInstance(MysqlService())
            bind(BissService::class.java).toClass(BissServiceImpl::class.java)
        }
    }

    val injector = createInjector(models)

    val controller = injector.getInstance(SprintUIController::class.java)!!

    controller.hi()

}