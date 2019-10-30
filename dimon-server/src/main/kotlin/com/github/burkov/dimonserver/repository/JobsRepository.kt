package com.github.burkov.dimonserver.repository

import com.github.burkov.dimonserver.model.Job
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import org.springframework.stereotype.Repository


@Repository
interface JobsRepository : JpaRepository<Job, Long> {
    @Query("select distinct context from Jobs")
    fun listDistinctContexts(): Set<String>
}

//@Repository
//class MarketDataRepository {
//    fun getAll(stock: String): Flux<MarketData> {
//        return Flux.fromStream(Stream.generate { getMarketDataResponse(stock) })
//                .log()
//                .delayElements(Duration.ofSeconds(1))
//    }
//
//    fun getOne(stock: String): Mono<MarketData> {
//        return Mono.just(getMarketDataResponse(stock))
//    }
//
//    private fun getMarketDataResponse(stock: String): MarketData {
//        return MarketData(stock, Random.nextInt(100))
//    }
//}