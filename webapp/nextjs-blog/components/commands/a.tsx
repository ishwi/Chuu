import {useEffect, useState} from 'react';

export default function useLongPress(callback = () => {
}, normalPress = () => {
}, ms = 200) {
    const [startLongPress, setStartLongPress] = useState(false);
    const [cancelledCalled, setCancelled] = useState(true);

    useEffect(() => {
        let timerId;
        if (startLongPress) {
            timerId = setTimeout(callback, ms);
            setCancelled(false);
        } else {
            if (!cancelledCalled) {
                setCancelled(true);
                normalPress();
            }
            if (timerId) {

                clearTimeout(timerId);
            }
        }

        return () => {
            clearTimeout(timerId);
        };
    }, [callback, normalPress, ms, startLongPress, cancelledCalled]);

    return {
        onMouseDown: () => setStartLongPress(true),
        onMouseUp: () => {
            console.log("MouseUp");
            setStartLongPress(false)
        },
        onMouseLeave: () => setStartLongPress(false),
        onTouchStart: () => setStartLongPress(true),
        onTouchEnd: () => setStartLongPress(false),
    };
}
