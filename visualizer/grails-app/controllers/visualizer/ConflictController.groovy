package visualizer

import com.ideaflow.model.Conflict
import com.ideaflow.model.Resolution
import com.ideaflow.timeline.ConflictBand
import com.ideaflow.timeline.TimePosition

class ConflictController {

    def list() {
        List<ConflictBand> conflicts = []

		conflicts << createConflict(
			"Was the data format I was using going to work in the chart?",
			"It was using the wrong chart data format for multiple series.",
			new TimePosition(5, 50, 0), new TimePosition(6, 00, 3))

		conflicts << createConflict(
			"Why am I getting an IndexOutOfBoundsException when there's no data?",
			"Code expected there to be at least one color.",
			new TimePosition(7, 30, 0), new TimePosition(7, 54, 10))

		conflicts << createConflict(
			"Why isn't the chart showing up?",
			"Forgot to add chart name to dictionary",
			new TimePosition(8, 15, 0), new TimePosition(8, 33, 24))

		conflicts << createConflict(
			"Why are the bars overlapping?",
			"Need to adjust scale on axis manually",
			new TimePosition(8, 43, 0), new TimePosition(8, 54, 35))

        render(template: "list", model: [conflicts: conflicts])
    }

    def show() {
        //show the full details of a single conflict
    }

    def edit() {
        //change the details of a conflict
    }

	private ConflictBand createConflict(String question, String answer, TimePosition start, TimePosition end) {
		ConflictBand band = new ConflictBand()
		band.conflict = new Conflict(question)
		band.resolution = new Resolution(answer)
		band.setStartPosition(start)
		band.setEndPosition(end)
		return band
	}


}
