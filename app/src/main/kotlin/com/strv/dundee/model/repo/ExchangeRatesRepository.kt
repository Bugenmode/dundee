package com.strv.dundee.model.repo

import android.arch.lifecycle.LiveData
import com.strv.dundee.model.api.exchangerate.ExchangeRateApi
import com.strv.dundee.model.api.exchangerate.ExchangeRateResponse
import com.strv.dundee.model.cache.ExchangeRatesCache
import com.strv.dundee.model.entity.ExchangeRates
import com.strv.ktools.NetworkBoundResource
import com.strv.ktools.Resource
import com.strv.ktools.RetrofitCallLiveData
import com.strv.ktools.inject
import retrofit2.Response
import java.util.Calendar

class ExchangeRatesRepository {

	val cache by inject<ExchangeRatesCache>()

	private val exchangeRateApi by inject<ExchangeRateApi>()

	fun getExchangeRates(source: String, target: List<String>, liveDataToReuse: LiveData<Resource<ExchangeRates>>? = null) = object : NetworkBoundResource<ExchangeRates, ExchangeRateResponse>(liveDataToReuse) {
		override fun saveCallResult(item: ExchangeRateResponse) {
			cache.putRates(item.getExchangeRates(source))
		}

		override fun shouldFetch(data: ExchangeRates?): Boolean {
			return data?.date?.let{
				val calendar = Calendar.getInstance()
				calendar.time = it
				calendar.add(Calendar.DAY_OF_YEAR, 1)
				val calendarNow = Calendar.getInstance()
				calendar.get(Calendar.DAY_OF_YEAR) != calendarNow.get(Calendar.DAY_OF_YEAR)
			} ?: true
		}

		override fun loadFromDb(): LiveData<ExchangeRates> = cache.getRates(source)

		override fun createCall(): LiveData<Response<out ExchangeRateResponse>> {
			return RetrofitCallLiveData(exchangeRateApi.getExchangeRates(source, target))
		}

	}.getAsLiveData()
}