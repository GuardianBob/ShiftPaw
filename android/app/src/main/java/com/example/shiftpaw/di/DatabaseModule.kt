package com.example.shiftpaw.di

import android.content.Context
import androidx.room.Room
import com.example.shiftpaw.data.local.dao.EmployeeDao
import com.example.shiftpaw.data.local.dao.ImportedScheduleDao
import com.example.shiftpaw.data.local.dao.ShiftDao
import com.example.shiftpaw.data.local.database.ShiftPawDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): ShiftPawDatabase =
        Room.databaseBuilder(context, ShiftPawDatabase::class.java, "shiftpaw.db")
            .fallbackToDestructiveMigration()
            .build()

    @Provides
    fun provideEmployeeDao(db: ShiftPawDatabase): EmployeeDao = db.employeeDao()

    @Provides
    fun provideShiftDao(db: ShiftPawDatabase): ShiftDao = db.shiftDao()

    @Provides
    fun provideImportedScheduleDao(db: ShiftPawDatabase): ImportedScheduleDao = db.importedScheduleDao()
}
