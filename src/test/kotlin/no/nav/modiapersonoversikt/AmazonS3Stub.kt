package no.nav.modiapersonoversikt

import com.amazonaws.services.s3.AmazonS3
import com.amazonaws.services.s3.model.Bucket
import com.amazonaws.services.s3.model.CreateBucketRequest
import com.amazonaws.services.s3.model.PutObjectResult
import com.amazonaws.services.s3.model.S3Object
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import java.io.ByteArrayInputStream

private var buckets: MutableMap<String, MutableMap<String, String>> = mutableMapOf()

fun createS3Stub(): AmazonS3 {
    val bucketName = slot<String>()
    val key = slot<String>()
    val content = slot<String>()
    val createBucketRequest = slot<CreateBucketRequest>()

    return mockk {
        every { getObject(capture(bucketName), capture(key)) } answers {
            val s3Object = S3Object()
            val bytes = buckets[bucketName.captured]?.get(key.captured)?.toByteArray() ?: ByteArray(0)

            s3Object.setObjectContent(ByteArrayInputStream(bytes))
            s3Object
        }
        every { putObject(capture(bucketName), capture(key), capture(content)) } answers {
            buckets[bucketName.captured]?.put(key.captured, content.captured)
            PutObjectResult()
        }
        every { listBuckets() } returns buckets.keys.map { Bucket(it) }
        every { createBucket(capture(createBucketRequest)) } answers {
            val request = createBucketRequest.captured
            val name = request.bucketName
            buckets.putIfAbsent(name, mutableMapOf())
            Bucket(name)
        }
    }
}