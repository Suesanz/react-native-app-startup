import React, {useEffect, useState} from 'react';
import {NativeEventEmitter, NativeModules, SafeAreaView, ScrollView, StyleSheet, Text, View,} from 'react-native';

const markersTime: { name: string, time: number }[] = [];
const emitter = new NativeEventEmitter(NativeModules.AppStartup);

// @ts-ignore
const getTime = (time: number) => global.performance.now() - time
const getMarkerName = (name: string) => {
  let formattedName = ''
  for (let i = 0; i < name.length; i++) {
    const char = name.charAt(i)
    if (char === char.toUpperCase()) {
      formattedName += ` ${char}`
    } else {
      if (i === 0) {
        formattedName += char.toUpperCase()
      } else {
        formattedName += char
      }
    }
  }

  return formattedName
}

const App = () => {
  const [timeMarkers, setTimeMarkers] = useState<typeof markersTime>([])

  useEffect(() => {
    (async () => {
      const nativeAppStartupTime = await NativeModules.AppStartup.getAppStartupTime()
      // const markersList: string = await NativeModules.AppStartup.getReactMarkersList()
      // const formattedMarkersList = markersList.slice(1, markersList.length - 1).split(', ')

      // Markers are only for JS based events
      markersTime.push({name: 'Native app start time', time: nativeAppStartupTime.startupTime})
      emitter.addListener('mark', (data) => {
        markersTime.push({
          name: getMarkerName(data.name),
          time: getTime(data.startTime)
        })
        if (markersTime.length === 31) {
          setTimeMarkers(markersTime)
        }

      })
      console.log('App startup time: ', nativeAppStartupTime);
    })()

    return () => {
      emitter.removeAllListeners('mark')
    }
  }, [])
  return (
    <SafeAreaView style={{flex: 1, padding: 10}}>
      <ScrollView>
        {timeMarkers.map(marker => <View key={marker.name} style={styles.markerContainer}>
          <Text style={styles.markerName}>{marker.name}</Text>
          <Text style={styles.markerName}>{marker.time}</Text>
        </View>)}
      </ScrollView>
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  markerName: {
    color: 'black',
    fontSize: 20
  },
  markerContainer: {
    flex: 1,
    flexDirection: 'row',
    justifyContent: 'space-between'
  }
})

export default App;
