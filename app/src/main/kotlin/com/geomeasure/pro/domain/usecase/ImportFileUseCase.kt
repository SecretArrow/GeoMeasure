package com.geomeasure.pro.domain.usecase

import android.content.Context
import android.net.Uri
import com.geomeasure.pro.data.import_data.FileImporter
import com.geomeasure.pro.data.local.db.entities.ProjectEntity
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject

class ImportFileUseCase @Inject constructor(
    private val fileImporter: FileImporter
) {
    suspend operator fun invoke(uri: Uri, context: Context): Result<ProjectEntity> =
        fileImporter.importFile(uri, context)
}
