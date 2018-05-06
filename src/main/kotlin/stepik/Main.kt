package stepik

import retrofit2.http.GET
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Query
import java.util.*

class Meta {
    var has_next: Boolean? = null
}

class Course {
    var learners_count: Int? = null
    var title: String? = null
}

class StepikResponse {
    var meta: Meta? = null
    var courses: List<Course>? = null
}

interface StepikAPI {
    @GET("/api/courses")
    fun getCourses(@Query("page") number: Int): Call<StepikResponse>
}


fun main(args: Array<String>) {
    var nTop = 10
    if (args.size == 1) {
        try {
            nTop = args[0].toInt()
        } catch (e: Exception) {
        }
    }
    val retrofit = Retrofit.Builder()
            .baseUrl("https://stepik.org")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    val api: StepikAPI = retrofit.create(StepikAPI::class.java)
    var pageNumber = 1
    var response = api.getCourses(pageNumber).execute()
    assert(response.code() != 200)
    var responseBody = response.body()

    val top = TreeSet<Course>({ o1, o2 -> o2.learners_count!!.compareTo(o1.learners_count!!) })
    while (responseBody!!.meta!!.has_next!!) {
        for (course in responseBody.courses!!) {
            if (top.size == nTop) {
                if (top.last().learners_count!! < course.learners_count!!) {
                    top.add(course)
                    top.remove(top.last())
                }
            } else {
                top.add(course)
            }
        }
        pageNumber++
        response = api.getCourses(pageNumber).execute()
        assert(response.code() != 200)
        responseBody = response.body()

    }
    var number = 1
    for (course in top) {
        println("""${number++}. ${course.title}. Количество студентов: ${course.learners_count}""")
    }

}