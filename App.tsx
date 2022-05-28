import React, {useEffect} from 'react';
import {SafeAreaView, NativeModules, View,} from 'react-native';

const App = () => {
  useEffect(() => {
    (async () => {
      const time = await NativeModules.AppStartup.getAppStartupTime()
      console.log('App startup time: ', time);
    })()
  }, [])
  return (
    <SafeAreaView style={{}}>
      <View>

      </View>
    </SafeAreaView>
  );
};

export default App;
