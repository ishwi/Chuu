// Hook
import useLocalStorage from './UseLocalStorage';

function useDarkMode() {
  const [enabledState, setEnabledState] = useLocalStorage('user-info');

  return [enabledState, setEnabledState];
}
