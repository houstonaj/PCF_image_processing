// Export cell measurments for a single image

import qupath.lib.objects.PathCellObject
import qupath.lib.scripting.QP

// Output directory
def outputDir = buildFilePath(PROJECT_BASE_DIR, "exports")
mkdirs(outputDir)

def includeAnnotationsOnly = true

// Marker channels thresholds and measurement type
def markerSettings = [
    'Pan-Cytokeratin' : [threshold: 50, measurementType:'cell'],
    'Ki67'            : [threshold: 20, measurementType:'cell'],
    'CD45'            : [threshold: 15, measurementType:'cell'],
    'CD3e'            : [threshold: 50, measurementType:'cell'],
    'CD4'             : [threshold: 40, measurementType:'cell'],
    'CD8'             : [threshold: 35, measurementType:'cell'],
    'CD11c'           : [threshold: 8, measurementType:'cell'],
    'CD14'            : [threshold: 40, measurementType:'cell'],
    'CD68'            : [threshold: 50, measurementType:'cell'],
    'HLA-A'           : [threshold: 50, measurementType:'cell'],
    'HLA-DR'          : [threshold: 15, measurementType:'cell'],
    'CD206'           : [threshold: 50, measurementType:'cell'],
    'CD163'           : [threshold: 8, measurementType:'cell'],
]

// Get current image
def imageData = QP.getCurrentImageData()
if (imageData == null) {
    QP.getLogger().error("No image open!")
    return
}
def imageName = imageData.getServer().getMetadata().getName()
def safeImageName = imageName.replaceAll("[^a-zA-Z0-9_\\-]", "_") // make safe for filename

// Collect cells
def cells
if (includeAnnotationsOnly) {
    def annotations = imageData.getHierarchy().getAnnotationObjects()
    cells = annotations.collectMany { ann ->
        ann.getChildObjects().findAll { it instanceof PathCellObject }
    }
} else {
    cells = imageData.getHierarchy().getFlattenedObjectList(null).findAll { it instanceof PathCellObject }
}

if (cells.isEmpty()) {
    QP.getLogger().error("No cells found!")
    return
}

// Prepare CSV
def header = ['Cell_ID','Image','Sample','Centroid_X','Centroid_Y']
markerSettings.keySet().each { m -> header << 'Mean_' + m }
markerSettings.keySet().each { m -> header << m + '_flag' }

def lines = []
lines << header.join(',')

def cellID = 1

cells.each { cell ->
    def row = []

    // Cell ID
    row << cellID++
    // Image
    row << imageName
    // Sample / parent annotation name
    def parentAnn = cell.getParent()
    def sampleName = parentAnn != null ? parentAnn.getName() : "NoParent"
    row << sampleName
    // Centroid X/Y
    def roi = cell.getROI()
    row << roi.getCentroidX()
    row << roi.getCentroidY()
    
    // Raw mean intensity
    markerSettings.each { marker, settings ->
        def measurementName = settings.measurementType == 'nucleus' ?
            "Nucleus: ${marker}: Mean" :
            "Cell: ${marker}: Mean"
        def val = cell.getMeasurementList().get(measurementName)
        row << (val != null ? val : "")
    }

    // Single-marker flags, for positivity
    markerSettings.each { marker, settings ->
        def measurementName = settings.measurementType == 'nucleus' ?
            "Nucleus: ${marker}: Mean" :
            "Cell: ${marker}: Mean"
        def val = cell.getMeasurementList().get(measurementName)
        row << ((val != null && val >= settings.threshold) ? 1 : 0)
    }

    lines << row.join(',')
}

// Write CSV
def outputFile = buildFilePath(outputDir, "${safeImageName}_cell_means_w_flag.csv")
new File(outputFile).text = lines.join('\n')
println "Export complete: ${lines.size()-1} cells saved to ${outputFile}"

