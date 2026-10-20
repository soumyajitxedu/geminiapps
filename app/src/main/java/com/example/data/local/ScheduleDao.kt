package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.ScheduleEventEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {

    @Query("SELECT * FROM schedule_events ORDER BY time ASC, id ASC")
    fun getAllEvents(): Flow<List<ScheduleEventEntity>>

    @Query("SELECT * FROM schedule_events WHERE date = :date ORDER BY time ASC, id ASC")
    fun getEventsForDate(date: String): Flow<List<ScheduleEventEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvent(event: ScheduleEventEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertEvents(events: List<ScheduleEventEntity>)

    @Update
    suspend fun updateEvent(event: ScheduleEventEntity)

    @Delete
    suspend fun deleteEvent(event: ScheduleEventEntity)

    @Query("DELETE FROM schedule_events WHERE id = :id")
    suspend fun deleteEventById(id: Long)

    @Query("UPDATE schedule_events SET isCompleted = :completed WHERE id = :id")
    suspend fun setEventCompleted(id: Long, completed: Boolean)

    @Query("UPDATE schedule_events SET isGoogleSynced = 1, googleEventId = :googleEventId WHERE id = :id")
    suspend fun markAsGoogleSynced(id: Long, googleEventId: String)

    @Query("UPDATE schedule_events SET isActive = CASE WHEN id = :activeId THEN 1 ELSE 0 END WHERE date = :date")
    suspend fun setActiveEventForDate(date: String, activeId: Long)

    @Query("SELECT COUNT(*) FROM schedule_events")
    suspend fun getEventCount(): Int
}
