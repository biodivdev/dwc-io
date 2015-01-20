(ns dwc-io.fixes-test
  (:use dwc-io.fixes
        midje.sweet))

(fact "Coordinates to decimal"
  (coord2decimal "38 53 55N") => 38.89861297607422
  (coord2decimal "38d 53 55 N") => 38.89861297607422
  (coord2decimal "38d 53' 55'' S") => -38.89861297607422
  (coord2decimal "77 2 16W") => -77.03778076171875)


(fact "It can fix coords systems"
  (fix-coords {:latitude "38d 53 55N" :longitude "77 2' 16'' W"})
      => {:latitude "38d 53 55N" :longitude "77 2' 16'' W" 
          :decimalLatitude 38.89861297607422 :decimalLongitude -77.03778076171875}
  (fix-coords {:latitude "38d 53 55N"})
      => {:latitude "38d 53 55N" }
  (fix-coords {:decimalLatitude 38.89861297607422 :decimalLongitude -77.03778076171875})
      => {:decimalLatitude 38.89861297607422 :decimalLongitude -77.03778076171875}
  (fix-verbatim-coords {:verbatimLatitude "10.10" :verbatimLongitude "20.20"})
      => {:verbatimLatitude "10.10" :verbatimLongitude "20.20"
          :decimalLatitude "10.10" :decimalLongitude "20.20"}
  (fix-coords
    (fix-verbatim-coords {:verbatimLatitude "38d 53 55N" :verbatimLongitude "77 2' 16'' W"}))
      => {:verbatimLatitude "38d 53 55N" :verbatimLongitude "77 2' 16'' W"
          :latitude "38d 53 55N" :longitude "77 2' 16'' W"
          :decimalLatitude 38.89861297607422 :decimalLongitude -77.03778076171875}
      )

(fact "Fixes occurrenceID"
  (fix-id {:occurrenceID "123" :id "321"})
    => {:occurrenceID "123" :id "321"}
  (fix-id {:id "321"})
    => {:occurrenceID "321" :id "321"}
  (fix-id {:globalUniqueIdentifier "321"})
    => {:occurrenceID "321" :globalUniqueIdentifier "321"}
  (fix-id {:institutionCode "inst" :collectionCode "col" :catalogNumber "num"} )
    => {:institutionCode "inst" :collectionCode "col" :catalogNumber "num" :occurrenceID "urn:occurrence:inst:col:num"}
  )

(fact "Fix decimal lat lng"
 (fix-decimal-lat {:decimalLatitude "10.10" :decimalLongitude "20.20"})
  => {:decimalLatitude 10.10 :decimalLongitude "20.20"}
 (fix-decimal-long {:decimalLatitude "10.10" :decimalLongitude "20.20"})
  => {:decimalLatitude "10.10" :decimalLongitude 20.20}
      )

(fact "Fix strings" 
  (fix-strings {:id 123 :recordNumber nil}) 
    => {:id "123"})

(fact "Fix keys"
  (fix-keys {:Id "123" :RecordNumber "321"})
    => {:id "123" :recordNumber "321"})
(fact "Only allowed fields"
   (fix-fields {:recordNumber "23" :foo "bar"})
      => {:recordNumber "23"})

(fact "apply many fixes"
  (-fix-> {:id "123"})
      => {:occurrenceID "123" :id "123"}
  (-fix-> [{:id "123"} {:id "321"}])
      => [{:occurrenceID "123" :id "123"} {:occurrenceID "321" :id "321"}]
  (-fix-> {:GlobalUniqueIdentifier 123 :decimalLatitude "10.10" :decimalLongitude "20.20" :recordNumber ""})
      => {:occurrenceID "123" :globalUniqueIdentifier "123" :decimalLatitude 10.10 :decimalLongitude 20.20}
  (-fix-> {:id 123 :decimalLatitude "10.10" :decimalLongitude "20.20" :RecordNumber "" :day 10 :Month "11.0"})
      => {:occurrenceID "123" :id "123" :decimalLatitude 10.10 :decimalLongitude 20.20 :day "10" :month "11"}
  )

(fact "Fix .0"
  (fix-dot-zero {:recordNumber "23.0" :foo "129.0.0.1"})
   => {:recordNumber "23" :foo "129.0.0.1"})

#_(fact "Fix names"
  (fix-naming {:scientificName "Auranticaria something   var. onthing"})
   => {:scientificName "Auranticaria something var. onthing"}
  (fix-naming {:scientificName "Aphelandra longiflora"})
   => {:scientificName "Aphelandra longiflora"}
  (fix-naming {:scientificName "Aphelandra longiflora" :scientificNameAuthorship "S.Profice"})
   => {:scientificName "Aphelandra longiflora S.Profice" :scientificNameAuthorship "S.Profice" :scientificNameWithoutAuthorship "Aphelandra longiflora"}
  (fix-naming {:scientificNameWithoutAuthorship "Aphelandra longiflora" :scientificNameAuthorship "S.Profice"})
   => {:scientificName "Aphelandra longiflora S.Profice" :scientificNameAuthorship "S.Profice" :scientificNameWithoutAuthorship "Aphelandra longiflora"}
)

(fact "It work with empty and nil"
  (-fix-> {}) => nil
  (-fix-> nil) => nil
  (-fix-> []) => []
  (-fix-> [nil]) => []
      )