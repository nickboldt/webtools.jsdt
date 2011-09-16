/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/


#pragma once

#include "activdbg.h"
#include <msdbg.h>
#include <psapi.h>
#include <vector>

#include "CrossfireEvent.h"
#include "CrossfireLineBreakpoint.h"
#include "CrossfireRequest.h"
#include "CrossfireResponse.h"
#include "IBreakpointTarget.h"
#include "IECrossfireServer.h"
#include "JSEvalCallback.h"
#include "Value.h"
#include "Logger.h"

class CrossfireContext; // forward declaration
#include "PendingScriptLoad.h"
#include "CrossfireServer.h"
#include "IEDebugger.h"

class CrossfireContext : IBreakpointTarget, IJSEvalHandler {

public:
	CrossfireContext(DWORD processId, wchar_t* url, CrossfireServer* server);
	~CrossfireContext();
	virtual void breakpointHit(IRemoteDebugApplicationThread *pDebugAppThread, BREAKREASON br, IActiveScriptErrorDebug *pScriptErrorDebug);
	virtual bool getDebugApplication(IRemoteDebugApplication** _value);
	virtual IDebugApplicationNode* getLastInitializedScriptNode();
	virtual wchar_t* getName();
	virtual DWORD getProcessId();
	virtual wchar_t* getUrl();
	virtual void installBreakpoints(std::vector<Value*>* breakpoints);
	virtual bool performRequest(CrossfireRequest* request);
	virtual bool scriptInitialized(IDebugApplicationNode *applicationNode);
	virtual bool scriptLoaded(IDebugApplicationNode *applicationNode);

	/* IBreakpointTarget methods */
	virtual bool breakpointAttributeChanged(unsigned int handle, wchar_t* name, Value* value);
	virtual bool deleteBreakpoint(unsigned int handle);
	virtual CrossfireBreakpoint* getBreakpoint(unsigned int handle);
	virtual void getBreakpoints(CrossfireBreakpoint*** ___values);
	virtual bool setBreakpoint(CrossfireBreakpoint* breakpoint);

	/* IJSEvalHandler methods */
	virtual void evalComplete(IDebugProperty* value, void* data);

private:
	struct JSObject {
		std::map<std::wstring, unsigned int> children;
		IDebugProperty* debugProperty;
		bool isObject;
		IDebugStackFrame* stackFrame;
	};

	virtual void clearObjects();
	virtual bool createValueForFrame(IDebugStackFrame* stackFrame, unsigned int frameIndex, bool includeScopes, Value** _value);
	virtual bool createValueForObject(JSObject* object, bool resolveChildObjects, Value** _value);
	virtual bool createValueForScript(IDebugApplicationNode* node, bool includeSource, bool failIfEmpty, Value** _value);
	virtual bool evaluate(IDebugStackFrame* stackFrame, wchar_t* expression, int flags, IDebugProperty** _result);
	virtual bool evaluateAsync(IDebugStackFrame* stackFrame, wchar_t* expression, int flags, IJSEvalHandler* handler, void* data);
	virtual bool getDebugApplicationThread(IRemoteDebugApplicationThread** _value);
	virtual bool getScriptUrl(IDebugApplicationNode* node, URL** _value);
	virtual IDebugApplicationNode* getScriptNode(URL* url);
	virtual bool hookDebugger();
	virtual bool registerScript(IDebugApplicationNode* applicationNode, bool recurse);
	virtual bool resumeFromBreak(BREAKRESUMEACTION action);
	virtual void sendEvent(CrossfireEvent* eventObj);
	virtual bool setBreakpointEnabled(CrossfireBreakpoint* breakpoint, bool enabled);
	virtual bool unhookDebugger();

	std::vector<JSEvalCallback*>* m_asyncEvals;
	std::map<unsigned int, CrossfireBreakpoint*>* m_breakpoints;
	DWORD m_cpcApplicationNodeEvents;
	IDebugApplicationNode* m_currentScriptNode;
	IRemoteDebugApplicationThread* m_debugApplicationThread;
	IIEDebugger* m_debugger;
	IDebugApplicationNode* m_lastInitializedScriptNode;
	bool m_debuggerHooked;
	wchar_t* m_name;
	unsigned int m_nextObjectHandle;
	std::map<unsigned int, JSObject*>* m_objects;
	std::map<IDebugApplicationNode*, PendingScriptLoad*>* m_pendingScriptLoads;
	DWORD m_processId;
	bool m_running;
	std::map<std::wstring, IDebugApplicationNode*>* m_scriptNodes;
	CrossfireServer* m_server;
	wchar_t* m_url;

	/* command: backtrace */
	static const wchar_t* COMMAND_BACKTRACE;
	static const wchar_t* KEY_FRAMES;
	static const wchar_t* KEY_FROMFRAME;
	static const wchar_t* KEY_TOFRAME;
	static const wchar_t* KEY_TOTALFRAMES;
	virtual int commandBacktrace(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: continue */
	static const wchar_t* COMMAND_CONTINUE;
	virtual int commandContinue(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: evaluate */
	static const wchar_t* COMMAND_EVALUATE;
	static const wchar_t* KEY_EXPRESSION;
	static const wchar_t* KEY_RESULT;
	virtual int commandEvaluate(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: frame */
	static const wchar_t* COMMAND_FRAME;
	static const wchar_t* KEY_FRAME;
	static const wchar_t* KEY_INDEX;
	virtual int commandFrame(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: inspect */
	static const wchar_t* COMMAND_INSPECT;

	/* command: lookup */
	static const wchar_t* COMMAND_LOOKUP;
	static const wchar_t* KEY_HANDLES;
	static const wchar_t* KEY_VALUES;
	virtual int commandLookup(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: scopes */
	static const wchar_t* COMMAND_SCOPES;
	static const wchar_t* KEY_FROMSCOPE;
	static const wchar_t* KEY_SCOPES;
	static const wchar_t* KEY_TOSCOPE;
	static const wchar_t* KEY_TOTALSCOPECOUNT;
	virtual int commandScopes(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: scripts */
	static const wchar_t* COMMAND_SCRIPTS;
	static const wchar_t* KEY_SCRIPTS;
	static const wchar_t* KEY_URLS;
	virtual int commandScripts(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* command: suspend */
	static const wchar_t* COMMAND_SUSPEND;
	static const wchar_t* KEY_STEPACTION;
	static const wchar_t* VALUE_IN;
	static const wchar_t* VALUE_NEXT;
	static const wchar_t* VALUE_OUT;
	virtual int commandSuspend(Value* arguments, Value** _responseBody, wchar_t** _message);

	/* event: onBreak */
	static const wchar_t* EVENT_ONBREAK;
	static const wchar_t* KEY_CAUSE;
	static const wchar_t* KEY_MESSAGE;
	static const wchar_t* KEY_TITLE;

	/* event: onResume */
	static const wchar_t* EVENT_ONRESUME;

	/* event: onScript */
	static const wchar_t* EVENT_ONSCRIPT;
	static const wchar_t* KEY_SCRIPT;

	/* event: onToggleBreakpoint */
	static const wchar_t* EVENT_ONTOGGLEBREAKPOINT;
	static const wchar_t* KEY_SET;

	/* shared */
	static const wchar_t* KEY_BREAKPOINT;
	static const wchar_t* KEY_CONTEXTID;
	static const wchar_t* KEY_FRAMEINDEX;
	static const wchar_t* KEY_HANDLE;
	static const wchar_t* KEY_INCLUDESCOPES;
	static const wchar_t* KEY_INCLUDESOURCE;
	static const wchar_t* KEY_LINE;
	static const wchar_t* KEY_LOCATION;
	static const wchar_t* KEY_TYPE;
	static const wchar_t* KEY_URL;

	/* breakpoint objects */
	static const wchar_t* BPTYPE_LINE;

	/* frame objects */
	static const wchar_t* KEY_FUNCTIONNAME;

	/* object objects */
	static const wchar_t* JSVALUE_BOOLEAN;
	static const wchar_t* JSVALUE_FUNCTION;
	static const wchar_t* JSVALUE_NUMBER;
	static const wchar_t* JSVALUE_NULL;
	static const wchar_t* JSVALUE_STRING;
	static const wchar_t* JSVALUE_TRUE;
	static const wchar_t* JSVALUE_UNDEFINED;
	static const wchar_t* KEY_LOCALS;
	static const wchar_t* KEY_THIS;
	static const wchar_t* KEY_VALUE;
	static const wchar_t* VALUE_BOOLEAN;
	static const wchar_t* VALUE_FUNCTION;
	static const wchar_t* VALUE_NUMBER;
	static const wchar_t* VALUE_OBJECT;
	static const wchar_t* VALUE_STRING;
	static const wchar_t* VALUE_UNDEFINED;

	/* script objects */
	static const wchar_t* KEY_COLUMNOFFSET;
	static const wchar_t* KEY_LINECOUNT;
	static const wchar_t* KEY_LINEOFFSET;
	static const wchar_t* KEY_SOURCE;
	static const wchar_t* KEY_SOURCELENGTH;
	static const wchar_t* VALUE_EVALCODE;
	static const wchar_t* VALUE_EVALLEVEL;
	static const wchar_t* VALUE_TOPLEVEL;

	/* other */
	static const wchar_t* ABOUT_BLANK;
	static const wchar_t* ID_PREAMBLE;
	static const wchar_t* NUMBER_INFINITY;
	static const wchar_t* NUMBER_NaN;
	static const wchar_t* NUMBER_NEGATIVEINFINITY;
	static const wchar_t* PDM_DLL;
	static const wchar_t* SCHEME_JSCRIPT;
	static const wchar_t* VALUE_INFINITY;
	static const wchar_t* VALUE_NaN;
	static const wchar_t* VALUE_NEGATIVEINFINITY;
};
