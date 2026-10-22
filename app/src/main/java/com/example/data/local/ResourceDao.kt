package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ResourceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ResourceDao {

    @Query("SELECT * FROM resources ORDER BY isUserAdded DESC, id ASC")
    fun getAllResources(): Flow<List<ResourceEntity>>

    @Query("SELECT * FROM resources WHERE subject = :subject ORDER BY isUserAdded DESC, id ASC")
    fun getResourcesBySubject(subject: String): Flow<List<ResourceEntity>>

    @Query("SELECT COUNT(*) FROM resources")
    suspend fun getResourceCount(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResources(resources: List<ResourceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertResource(resource: ResourceEntity): Long

    @Update
    suspend fun updateResource(resource: ResourceEntity)

    @Delete
    suspend fun deleteResource(resource: ResourceEntity)

    @Query("DELETE FROM resources WHERE id = :id")
    suspend fun deleteResourceById(id: Long)

    @Query("DELETE FROM resources")
    suspend fun clearAll()
}
