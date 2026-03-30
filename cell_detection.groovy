import qupath.ext.stardist.StarDist2D
import qupath.lib.scripting.QP

def modelPath = "QuPath-0.6.0/app/models/dsb2018_heavy_augment.pb" //change path to saved model

def stardist = StarDist2D
    .builder(modelPath)
    .channels('DAPI')            // Extract channel called 'DAPI'
    .normalizePercentiles(1, 99.5) // Percentile normalization
    .threshold(0.55)              // Probability (detection) threshold
    .pixelSize(0.5)              // Resolution for detection
    .cellExpansion(2)            // Don't expand nuclei to approximate cell boundaries
    .measureShape()              // Add shape measurements
    .measureIntensity()          // Single-channel
    .build()

// Get current image
def imageData = QP.getCurrentImageData()

// Define which objects will be used as the 'parents' for detection
// Use QP.getSelectedObjects() if you want to use only the selected objects
def annotations = QP.getAnnotationObjects()

// Filter annotations to only those classified as "Region"
def regionAnnotations = annotations.findAll { ann ->
    ann.getPathClass() != null &&
    ann.getPathClass().getName() == '' //add sample_ID
}

// Safety check
if (regionAnnotations.isEmpty()) {
    QP.getLogger().error("No annotations classified as 'Region' found!")
    return
}

// Run StarDist on Region annotations only
stardist.detectObjects(imageData, regionAnnotations)

// Clean up
stardist.close()
println "StarDist completed on ${regionAnnotations.size()} Region annotations."
