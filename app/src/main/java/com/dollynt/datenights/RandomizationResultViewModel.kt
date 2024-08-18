
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.dollynt.datenights.model.RandomizationResult
import com.dollynt.datenights.repository.RandomizationResultRepository
import kotlinx.coroutines.launch

class RandomizationResultViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = RandomizationResultRepository()

    private val _randomizationResults = MutableLiveData<List<RandomizationResult>>()
    val randomizationResults: LiveData<List<RandomizationResult>> get() = _randomizationResults

    fun fetchRandomizationResults(coupleId: String) {
        repository.getRandomizationResultsForCouple(
            coupleId,
            onSuccess = { results ->
                _randomizationResults.postValue(results)
            },
            onFailure = { e ->
                e.printStackTrace()
                _randomizationResults.postValue(emptyList())
            }
        )
    }
}
