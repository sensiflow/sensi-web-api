import com.isel.sensiflow.model.dao.Device
import com.isel.sensiflow.model.dao.ProcessedStream
import com.isel.sensiflow.model.dao.User
import com.isel.sensiflow.model.repository.DeviceRepository
import com.isel.sensiflow.model.repository.ProcessedStreamRepository
import com.isel.sensiflow.services.DeviceNotFoundException
import com.isel.sensiflow.services.OwnerMismatchException
import com.isel.sensiflow.services.ProcessedStreamNotFoundException
import com.isel.sensiflow.services.ProcessedStreamService
import com.isel.sensiflow.services.dto.output.DeviceSimpleOutputDTO
import com.isel.sensiflow.services.dto.output.ProcessedStreamExpandedOutputDTO
import com.isel.sensiflow.services.dto.output.toDTO
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.times
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner
import java.util.Optional

@RunWith(MockitoJUnitRunner::class)
class ProcessedStreamTests {

    @InjectMocks
    private lateinit var processedStreamService: ProcessedStreamService

    @Mock
    private lateinit var processedStreamRepository: ProcessedStreamRepository

    @Mock
    private lateinit var deviceRepository: DeviceRepository

    @BeforeEach
    fun initMocks() {
        MockitoAnnotations.openMocks(this)
    }

    private val fakeUser = User(
        id = 1,
        firstName = "John",
        lastName = "Doe",
        passwordHash = "hash",
        passwordSalt = "salt"
    )

    private val fakeDevice = Device(
        id = 1,
        name = "Device 1",
        streamURL = "https://example.com/device1/stream",
        description = "Device 1 description",
        user = fakeUser
    )

    private val fakeDevice2 = Device(
        id = 2,
        name = "Device 2",
        streamURL = "https://example.com/device2/stream",
        description = "Device 2 description",
        user = fakeUser
    )

    private val fakeProcessedStream = ProcessedStream(
        id = 1,
        device = fakeDevice,
        streamURL = "https://example.com/device1/processedStream",
    )

    @Test
    fun `get a simple processed stream of a device`() {
        // Arrange
        val deviceId = 1
        val userId = 1
        val expanded = false

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))
        `when`(processedStreamRepository.findById(deviceId)).thenReturn(Optional.of(fakeProcessedStream))

        // Act
        val processedStream = processedStreamService.getProcessedStreamOfDeviceWith(deviceId, userId, expanded)

        // Assert
        assertEquals(fakeProcessedStream.toDTO(expanded), processedStream)
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(processedStreamRepository, times(1)).findById(deviceId)
    }

    @Test
    fun `get an expanded processed stream of a device`() {
        // Arrange
        val deviceId = 1
        val userId = 1
        val expanded = true

        val expandedProcessedStream = ProcessedStreamExpandedOutputDTO(
            streamUrl = fakeProcessedStream.streamURL,
            device = DeviceSimpleOutputDTO(
                id = fakeDevice.id,
                name = fakeDevice.name,
                streamURL = fakeDevice.streamURL,
                description = fakeDevice.description,
                userID = fakeDevice.user.id
            )
        )

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))
        `when`(processedStreamRepository.findById(deviceId)).thenReturn(Optional.of(fakeProcessedStream))

        // Act
        val processedStream = processedStreamService.getProcessedStreamOfDeviceWith(deviceId, userId, expanded)

        // Assert
        assertEquals(expandedProcessedStream, processedStream)
        verify(deviceRepository, times(1)).findById(deviceId)
        verify(processedStreamRepository, times(1)).findById(deviceId)
    }

    @Test
    fun `get processed stream of a device that does not belong to the user`() {
        // Arrange
        val deviceId = 1
        val userId = fakeUser.id + 1
        val expanded = false

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice))
        `when`(processedStreamRepository.findById(deviceId)).thenReturn(Optional.of(fakeProcessedStream))

        // Act
        assertThrows<OwnerMismatchException> {
            processedStreamService.getProcessedStreamOfDeviceWith(deviceId, userId, expanded)
        }

        verify(deviceRepository, times(1)).findById(deviceId)
        verify(processedStreamRepository, times(0)).findById(anyInt())
    }

    @Test
    fun `get processed stream of a device that does not exist`() {
        // Arrange
        val nonExistentDeviceID = 1
        val userId = 1
        val expanded = false

        `when`(deviceRepository.findById(nonExistentDeviceID)).thenReturn(Optional.empty())

        // Act
        assertThrows<DeviceNotFoundException> {
            processedStreamService.getProcessedStreamOfDeviceWith(nonExistentDeviceID, userId, expanded)
        }

        verify(deviceRepository, times(1)).findById(nonExistentDeviceID)
        verify(processedStreamRepository, times(0)).findById(anyInt())
    }

    @Test
    fun `get a non existent processed stream`() {
        // Arrange
        val deviceId = 2
        val userId = 1
        val expanded = false

        `when`(deviceRepository.findById(deviceId)).thenReturn(Optional.of(fakeDevice2))
        `when`(processedStreamRepository.findById(deviceId)).thenReturn(Optional.empty())

        // Act
        assertThrows<ProcessedStreamNotFoundException> {
            processedStreamService.getProcessedStreamOfDeviceWith(deviceId, userId, expanded)
        }

        verify(deviceRepository, times(1)).findById(deviceId)
        verify(processedStreamRepository, times(1)).findById(anyInt())
    }
}
