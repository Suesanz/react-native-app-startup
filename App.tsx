import React, {useEffect, useState} from 'react';
import {
  NativeEventEmitter,
  NativeModules,
  SafeAreaView,
  ScrollView,
  StyleSheet,
  Text,
  TextStyle,
  View,
} from 'react-native';

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

interface AppStartupTime {
  nativeAppStartupTime: number
  totalStartupTime: number
  isColdStart: boolean
  didFetchAppStart: boolean
}

const getTimeInSecs = (time: number) => {
  return `${Number(time / 1000000).toFixed(2)}s`
}
const App = () => {
  const [timeMarkers, setTimeMarkers] = useState<typeof markersTime>([])
  const [appStartupTime, setAppStartupTime] = useState<AppStartupTime>()

  useEffect(() => {
    (async () => {
      emitter.addListener('mark', (data) => {
        markersTime.push({
          name: getMarkerName(data.name),
          time: getTime(data.startTime)
        })
        if (markersTime.length === 31) {
          setTimeMarkers(markersTime)
        }
      })

      if (!appStartupTime?.didFetchAppStart) {
        const appStartupTimeData: AppStartupTime = await NativeModules.AppStartup.getAppStartupTime()
        setAppStartupTime(appStartupTimeData)
      }

    })()
    return () => {
      emitter.removeAllListeners('mark')
    }
  }, [])

  return appStartupTime ? (
    <SafeAreaView style={{flex: 1, padding: 8}}>
      <ScrollView showsVerticalScrollIndicator={false}>
        <Text style={styles.heading}>App startup time</Text>
        <View style={styles.appStartupContainer}>
          <View style={styles.appStartupContainerSection}>
            <Text style={styles.appStartupText}>Total app startup time</Text>
            <Text
              style={styles.appStartupText}>{getTimeInSecs(appStartupTime.totalStartupTime)}</Text>
          </View>
          <View style={styles.appStartupContainerSection}>
            <Text style={styles.appStartupText}>Native app startup time</Text>
            <Text
              style={styles.appStartupText}>{getTimeInSecs(appStartupTime.nativeAppStartupTime)}</Text>
          </View>
          <View style={styles.appStartupContainerSection}>
            <Text style={styles.appStartupText}>Cold start</Text>
            <Text style={styles.appStartupText}>{`${appStartupTime?.isColdStart}`}</Text>
          </View>
        </View>

        {timeMarkers.length ? <Text style={styles.heading}>Breakdown of JS startup time</Text> : null}
        {timeMarkers.map(marker => <View key={marker.name} style={styles.markerContainer}>
          <Text style={styles.markerName}>{marker.name}</Text>
          <Text style={styles.markerTime}>{marker.time}ms</Text>
        </View>)}
      </ScrollView>
    </SafeAreaView>
  ) : null;
};

const styles = StyleSheet.create({
  markerName: {
    color: 'black',
    fontSize: 18,
    width: 200,
    marginHorizontal: 6
  } as TextStyle,

  markerTime: {
    color: 'black',
    fontSize: 18,
    marginHorizontal: 6
  },
  heading: {
    color: 'black',
    fontSize: 22,
    marginVertical: 16,
    fontWeight: 'bold',
    marginHorizontal: 6
  },
  appStartupText: {
    color: 'black',
    fontSize: 18,
    marginHorizontal: 6
  },
  appStartupContainerSection: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginHorizontal: 10,
    marginVertical: 4,
  },
  appStartupContainer: {
    flex: 1,
    height: 100,
    marginVertical: 4,
    borderWidth: 0.2,
    borderRadius: 5,
    backgroundColor: 'white',
    justifyContent: 'center'
  },
  markerContainer: {
    flex: 1,
    height: 50,
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginVertical: 4,
    borderWidth: 0.2,
    alignItems: 'center',
    borderRadius: 5,
    backgroundColor: 'white',
  }
})

export default App;
