package ambilight

/**
 * Created by David Khol [david@khol.me] on 20. 7. 2017.
 */
interface GUIListener {

	var isLivePreview: Boolean
	var renderRate: Long
	var updateRate: Long
	var smoothness: Int
	var saturation: Double
	var brightness: Float
	var cutOff: Int
	var temperature: Int
}
