import React, {useContext, useReducer} from 'react';

import {IGlobalStatus} from '../types/global.types';


export const GlobalMessagingContext = React.createContext({});

const initialState: IGlobalStatus = {message: ''};

enum ActionType {
    SetDetails = 'setMessage',
    RemoveDetails = 'removeMessage'
}

interface IAction {
    type: ActionType;
    payload: IGlobalStatus;
}

const reducer: React.Reducer<{}, IAction> = (state, action) => {
    switch (action.type) {
        case ActionType.SetDetails:
            return {
                message: action.payload.message
            };
        case ActionType.RemoveDetails:
            return {
                message: initialState.message
            };
        default:
            throw new Error(`Unhandled action type: ${action.type}`);
    }
};

export const GlobalMessagingProvider = ({children}: any) => {
    const [state, dispatch] = useReducer(reducer, initialState);

    return (
        <GlobalMessagingContext.Provider value={[state, dispatch]}>
            {children}
        </GlobalMessagingContext.Provider>
    );
};

// useContext hook - export here to keep code for global auth state
// together in this file, allowing user info to be accessed and updated
// in any functional component using the hook
export const useGlobalMessaging: any = () => useContext(GlobalMessagingContext);
