package com.aaron.chen.animeone.module.datastore

interface IAnimePrefRepository: IBasePrefDataStore  {
    val lastReviewTriggerTime: DataStoreReadWriteDelegate<String>
    val animeListClickCount: DataStoreReadWriteDelegate<Int>
    val hasReviewInviteTriggered: DataStoreReadWriteDelegate<Boolean>
}